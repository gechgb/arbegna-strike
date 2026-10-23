package com.example.lan

import android.util.Log
import com.example.model.MapArena
import com.example.model.WarriorHero
import com.example.model.WeaponType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class RemotePlayerPacket(
    val id: String,
    val name: String,
    val hero: WarriorHero,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var aimAngleDeg: Float,
    var isFacingRight: Boolean,
    var isJetpacking: Boolean,
    var currentWeapon: WeaponType,
    var health: Int,
    var armor: Int,
    var isDead: Boolean,
    var kills: Int = 0,
    var deaths: Int = 0
)

data class NetworkFireEvent(
    val shooterId: String,
    val weaponType: WeaponType,
    val x: Float,
    val y: Float,
    val aimAngleDeg: Float
)

data class NetworkHitEvent(
    val attackerId: String,
    val victimId: String,
    val damage: Int,
    val weaponType: WeaponType
)

data class DiscoveredHost(
    val ip: String,
    val hostName: String,
    val arenaTitle: String,
    val playerCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class LanLobbyPlayer(
    val id: String,
    val name: String,
    val hero: WarriorHero,
    val isHost: Boolean,
    var isReady: Boolean = false
)

class NetworkManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var serverJob: Job? = null
    private var clientJob: Job? = null
    private var udpServerJob: Job? = null
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var clientWriter: PrintWriter? = null
    private var udpSocket: java.net.DatagramSocket? = null

    // For Host: list of client handlers
    private val clientHandlers = CopyOnWriteArrayList<ClientHandler>()

    // Lobby & Connected players
    val lobbyPlayers = CopyOnWriteArrayList<LanLobbyPlayer>()
    val remotePlayers = ConcurrentHashMap<String, RemotePlayerPacket>()
    val incomingFireEvents = CopyOnWriteArrayList<NetworkFireEvent>()
    val incomingHitEvents = CopyOnWriteArrayList<NetworkHitEvent>()
    val incomingChatMessages = CopyOnWriteArrayList<Pair<String, String>>()
    val discoveredHosts = CopyOnWriteArrayList<DiscoveredHost>()

    var isHost: Boolean = false
    var isConnected: Boolean = false
    var isMatchStarted: Boolean = false
    var selectedArena: MapArena = MapArena.SIMIEN_PEAKS

    var onMatchStartedCallback: ((MapArena) -> Unit)? = null
    var onPlayerListUpdated: (() -> Unit)? = null
    var onDiscoveredHostsUpdated: (() -> Unit)? = null

    companion object {
        const val DEFAULT_PORT = 8888
        const val UDP_DISCOVERY_PORT = 8889
        const val HOTSPOT_DEFAULT_IP = "192.168.43.1"

        fun getDeviceIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                var hotspotIp = ""
                var wifiIp = ""
                while (interfaces.hasMoreElements()) {
                    val networkInterface = interfaces.nextElement()
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            val ip = address.hostAddress ?: ""
                            val name = networkInterface.name.lowercase()
                            if (name.contains("wlan") || name.contains("eth")) {
                                wifiIp = ip
                            } else if (name.contains("ap") || name.contains("swlan") || ip.startsWith("192.168.43")) {
                                hotspotIp = ip
                            }
                        }
                    }
                }
                return if (hotspotIp.isNotEmpty()) hotspotIp else if (wifiIp.isNotEmpty()) wifiIp else "127.0.0.1"
            } catch (e: Exception) {
                return "127.0.0.1"
            }
        }
    }

    /**
     * Start hosting a match on the local network (Wi-Fi or Hotspot).
     */
    fun startHost(localPlayerName: String, localPlayerHero: WarriorHero, arena: MapArena) {
        stopAll()
        isHost = true
        isConnected = true
        isMatchStarted = false
        selectedArena = arena

        lobbyPlayers.clear()
        lobbyPlayers.add(
            LanLobbyPlayer(
                id = "host",
                name = localPlayerName,
                hero = localPlayerHero,
                isHost = true,
                isReady = true
            )
        )
        onPlayerListUpdated?.invoke()

        // 1. TCP Server for authoritative state and client connections
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(DEFAULT_PORT)
                Log.d("ArbegnaLAN", "Server started on port $DEFAULT_PORT, IP: ${getDeviceIpAddress()}")
                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    val handler = ClientHandler(socket)
                    clientHandlers.add(handler)
                    handler.start()
                }
            } catch (e: Exception) {
                Log.e("ArbegnaLAN", "Server error", e)
            }
        }

        // 2. UDP Discovery Responder for 1-click automatic LAN matching
        udpServerJob = scope.launch {
            try {
                val socket = java.net.DatagramSocket(UDP_DISCOVERY_PORT)
                udpSocket = socket
                val buffer = ByteArray(512)
                val packet = java.net.DatagramPacket(buffer, buffer.size)
                while (isActive) {
                    socket.receive(packet)
                    val received = String(packet.data, 0, packet.length).trim()
                    if (received == "DISCOVER_ARBEGNA") {
                        val hostIp = getDeviceIpAddress()
                        val responseStr = "ARBEGNA_HOST:$hostIp:$localPlayerName:${selectedArena.title}:${lobbyPlayers.size}"
                        val resBytes = responseStr.toByteArray()
                        val responsePacket = java.net.DatagramPacket(
                            resBytes,
                            resBytes.size,
                            packet.address,
                            packet.port
                        )
                        socket.send(responsePacket)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Broadcasts UDP ping across the local network to find all active Arbegna Strike hosts.
     */
    fun scanLanHosts(onFinished: () -> Unit) {
        discoveredHosts.clear()
        scope.launch {
            var scanSocket: java.net.DatagramSocket? = null
            try {
                scanSocket = java.net.DatagramSocket()
                scanSocket.broadcast = true
                scanSocket.soTimeout = 1200

                val pingMsg = "DISCOVER_ARBEGNA".toByteArray()
                // Broadcast to global broadcast
                val broadcastPacket = java.net.DatagramPacket(
                    pingMsg,
                    pingMsg.size,
                    java.net.InetAddress.getByName("255.255.255.255"),
                    UDP_DISCOVERY_PORT
                )
                scanSocket.send(broadcastPacket)

                // Also ping standard hotspot gateway in case subnet blocks broadcast
                try {
                    val hotspotPacket = java.net.DatagramPacket(
                        pingMsg,
                        pingMsg.size,
                        java.net.InetAddress.getByName(HOTSPOT_DEFAULT_IP),
                        UDP_DISCOVERY_PORT
                    )
                    scanSocket.send(hotspotPacket)
                } catch (_: Exception) {}

                val buffer = ByteArray(512)
                val responsePacket = java.net.DatagramPacket(buffer, buffer.size)
                val endTime = System.currentTimeMillis() + 1200L

                while (System.currentTimeMillis() < endTime) {
                    try {
                        scanSocket.receive(responsePacket)
                        val msg = String(responsePacket.data, 0, responsePacket.length).trim()
                        val parts = msg.split(":")
                        if (parts[0] == "ARBEGNA_HOST" && parts.size >= 5) {
                            val ip = parts[1]
                            val hostName = parts[2]
                            val arenaTitle = parts[3]
                            val pCount = parts[4].toIntOrNull() ?: 1
                            if (discoveredHosts.none { it.ip == ip }) {
                                discoveredHosts.add(
                                    DiscoveredHost(
                                        ip = ip,
                                        hostName = hostName,
                                        arenaTitle = arenaTitle,
                                        playerCount = pCount
                                    )
                                )
                                withContext(Dispatchers.Main) {
                                    onDiscoveredHostsUpdated?.invoke()
                                }
                            }
                        }
                    } catch (_: java.net.SocketTimeoutException) {
                        break
                    }
                }
            } catch (_: Exception) {}
            finally {
                try { scanSocket?.close() } catch (_: Exception) {}
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    /**
     * Connect to a hosted match via IP address.
     */
    fun joinHost(hostIp: String, localPlayerName: String, localPlayerHero: WarriorHero, onConnected: (Boolean) -> Unit) {
        stopAll()
        isHost = false
        isMatchStarted = false

        clientJob = scope.launch {
            try {
                val socket = Socket(hostIp, DEFAULT_PORT)
                clientSocket = socket
                clientWriter = PrintWriter(socket.getOutputStream(), true)
                isConnected = true
                onConnected(true)

                // Send join greeting
                val joinMsg = "JOIN:client_${System.currentTimeMillis() % 10000}:$localPlayerName:${localPlayerHero.name}"
                clientWriter?.println(joinMsg)

                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isActive) {
                    val line = reader.readLine() ?: break
                    handleClientReceivedLine(line)
                }
            } catch (e: Exception) {
                Log.e("ArbegnaLAN", "Client connection error", e)
                isConnected = false
                onConnected(false)
            }
        }
    }

    fun launchMatchFromHost() {
        if (!isHost) return
        isMatchStarted = true
        broadcastToClients("START_MATCH:${selectedArena.name}")
        onMatchStartedCallback?.invoke(selectedArena)
    }

    fun broadcastPlayerSync(packet: RemotePlayerPacket) {
        val msg = "SYNC:${packet.id}:${packet.name}:${packet.hero.name}:${packet.x}:${packet.y}:${packet.vx}:${packet.vy}:${packet.aimAngleDeg}:${packet.isFacingRight}:${packet.isJetpacking}:${packet.currentWeapon.name}:${packet.health}:${packet.armor}:${packet.isDead}:${packet.kills}:${packet.deaths}"
        if (isHost) {
            broadcastToClients(msg)
        } else {
            clientWriter?.println(msg)
        }
    }

    fun broadcastFire(event: NetworkFireEvent) {
        val msg = "FIRE:${event.shooterId}:${event.weaponType.name}:${event.x}:${event.y}:${event.aimAngleDeg}"
        if (isHost) {
            broadcastToClients(msg)
        } else {
            clientWriter?.println(msg)
        }
    }

    fun broadcastHit(event: NetworkHitEvent) {
        val msg = "HIT:${event.attackerId}:${event.victimId}:${event.damage}:${event.weaponType.name}"
        if (isHost) {
            broadcastToClients(msg)
        } else {
            clientWriter?.println(msg)
        }
    }

    fun broadcastTaunt(senderName: String, text: String) {
        val msg = "CHAT:$senderName:$text"
        incomingChatMessages.add(Pair(senderName, text))
        if (isHost) {
            broadcastToClients(msg)
        } else {
            clientWriter?.println(msg)
        }
    }

    private fun broadcastToClients(message: String) {
        clientHandlers.forEach { handler ->
            try {
                handler.send(message)
            } catch (_: Exception) {}
        }
    }

    private fun handleClientReceivedLine(line: String) {
        val parts = line.split(":")
        when (parts[0]) {
            "START_MATCH" -> {
                val arenaName = parts.getOrNull(1) ?: MapArena.SIMIEN_PEAKS.name
                val arena = runCatching { MapArena.valueOf(arenaName) }.getOrDefault(MapArena.SIMIEN_PEAKS)
                isMatchStarted = true
                onMatchStartedCallback?.invoke(arena)
            }
            "LOBBY_UPDATE" -> {
                // Parse lobby players list: LOBBY_UPDATE:id,name,hero,isHost;id,name,hero,isHost...
                if (parts.size > 1) {
                    val listStr = parts[1]
                    val playerTokens = listStr.split(";")
                    val newList = playerTokens.mapNotNull { token ->
                        val sub = token.split(",")
                        if (sub.size >= 4) {
                            val hero = runCatching { WarriorHero.valueOf(sub[2]) }.getOrDefault(WarriorHero.ARBEGNA)
                            LanLobbyPlayer(sub[0], sub[1], hero, sub[3].toBoolean())
                        } else null
                    }
                    lobbyPlayers.clear()
                    lobbyPlayers.addAll(newList)
                    onPlayerListUpdated?.invoke()
                }
            }
            "SYNC" -> {
                if (parts.size >= 16) {
                    val id = parts[1]
                    val name = parts[2]
                    val hero = runCatching { WarriorHero.valueOf(parts[3]) }.getOrDefault(WarriorHero.ARBEGNA)
                    val x = parts[4].toFloatOrNull() ?: 0f
                    val y = parts[5].toFloatOrNull() ?: 0f
                    val vx = parts[6].toFloatOrNull() ?: 0f
                    val vy = parts[7].toFloatOrNull() ?: 0f
                    val aim = parts[8].toFloatOrNull() ?: 0f
                    val facing = parts[9].toBoolean()
                    val jet = parts[10].toBoolean()
                    val weapon = runCatching { WeaponType.valueOf(parts[11]) }.getOrDefault(WeaponType.SPARROW_BOW)
                    val hp = parts[12].toIntOrNull() ?: 100
                    val arm = parts[13].toIntOrNull() ?: 50
                    val dead = parts[14].toBoolean()
                    val kills = parts[15].toIntOrNull() ?: 0
                    val deaths = parts.getOrNull(16)?.toIntOrNull() ?: 0

                    remotePlayers[id] = RemotePlayerPacket(
                        id = id,
                        name = name,
                        hero = hero,
                        x = x,
                        y = y,
                        vx = vx,
                        vy = vy,
                        aimAngleDeg = aim,
                        isFacingRight = facing,
                        isJetpacking = jet,
                        currentWeapon = weapon,
                        health = hp,
                        armor = arm,
                        isDead = dead,
                        kills = kills,
                        deaths = deaths
                    )
                }
            }
            "FIRE" -> {
                if (parts.size >= 6) {
                    val shooterId = parts[1]
                    val weapon = runCatching { WeaponType.valueOf(parts[2]) }.getOrDefault(WeaponType.SPARROW_BOW)
                    val x = parts[3].toFloatOrNull() ?: 0f
                    val y = parts[4].toFloatOrNull() ?: 0f
                    val aim = parts[5].toFloatOrNull() ?: 0f
                    incomingFireEvents.add(NetworkFireEvent(shooterId, weapon, x, y, aim))
                }
            }
            "HIT" -> {
                if (parts.size >= 5) {
                    val att = parts[1]
                    val vic = parts[2]
                    val dmg = parts[3].toIntOrNull() ?: 20
                    val weapon = runCatching { WeaponType.valueOf(parts[4]) }.getOrDefault(WeaponType.SPARROW_BOW)
                    incomingHitEvents.add(NetworkHitEvent(att, vic, dmg, weapon))
                }
            }
            "CHAT" -> {
                if (parts.size >= 3) {
                    incomingChatMessages.add(Pair(parts[1], parts.subList(2, parts.size).joinToString(":")))
                }
            }
        }
    }

    private inner class ClientHandler(private val socket: Socket) {
        private var writer: PrintWriter? = null
        private var clientPlayerId: String = ""

        fun start() {
            scope.launch {
                try {
                    writer = PrintWriter(socket.getOutputStream(), true)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    // Send current lobby state immediately
                    sendLobbyUpdateToAll()

                    while (isActive) {
                        val line = reader.readLine() ?: break
                        handleServerReceivedLine(this@ClientHandler, line)
                    }
                } catch (_: Exception) {
                } finally {
                    clientHandlers.remove(this@ClientHandler)
                    lobbyPlayers.removeAll { it.id == clientPlayerId }
                    remotePlayers.remove(clientPlayerId)
                    sendLobbyUpdateToAll()
                    onPlayerListUpdated?.invoke()
                    try { socket.close() } catch (_: Exception) {}
                }
            }
        }

        fun send(message: String) {
            writer?.println(message)
        }

        fun setPlayerId(id: String) {
            clientPlayerId = id
        }
    }

    private fun handleServerReceivedLine(handler: ClientHandler, line: String) {
        val parts = line.split(":")
        when (parts[0]) {
            "JOIN" -> {
                val id = parts.getOrNull(1) ?: "player_${System.currentTimeMillis() % 1000}"
                val name = parts.getOrNull(2) ?: "Warrior"
                val hero = runCatching { WarriorHero.valueOf(parts.getOrNull(3) ?: "") }.getOrDefault(WarriorHero.ARBEGNA)
                handler.setPlayerId(id)
                lobbyPlayers.add(LanLobbyPlayer(id, name, hero, isHost = false, isReady = true))
                sendLobbyUpdateToAll()
                onPlayerListUpdated?.invoke()
            }
            "SYNC", "FIRE", "HIT", "CHAT" -> {
                // Host also processes it locally and echoes to other clients!
                handleClientReceivedLine(line)
                clientHandlers.forEach { other ->
                    if (other != handler) {
                        other.send(line)
                    }
                }
            }
        }
    }

    private fun sendLobbyUpdateToAll() {
        val payload = lobbyPlayers.joinToString(";") { "${it.id},${it.name},${it.hero.name},${it.isHost}" }
        val msg = "LOBBY_UPDATE:$payload"
        broadcastToClients(msg)
    }

    fun stopAll() {
        serverJob?.cancel()
        clientJob?.cancel()
        udpServerJob?.cancel()
        try { serverSocket?.close() } catch (_: Exception) {}
        try { clientSocket?.close() } catch (_: Exception) {}
        try { udpSocket?.close() } catch (_: Exception) {}
        clientHandlers.forEach { try { it.send("DISCONNECTED") } catch (_: Exception) {} }
        clientHandlers.clear()
        remotePlayers.clear()
        lobbyPlayers.clear()
        discoveredHosts.clear()
        incomingFireEvents.clear()
        incomingHitEvents.clear()
        isConnected = false
        isHost = false
        isMatchStarted = false
    }
}

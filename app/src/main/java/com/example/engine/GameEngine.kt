package com.example.engine

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.example.audio.GameAudio
import com.example.lan.NetworkFireEvent
import com.example.lan.NetworkHitEvent
import com.example.lan.NetworkManager
import com.example.lan.RemotePlayerPacket
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.*
import kotlin.random.Random

/**
 * 2D Action Platformer & Physics Engine for Arbegna Strike.
 * Handles dual-stick movement, jetpack flight, weapon progression, AI bot simulation,
 * ballistics, explosions, and LAN state synchronization.
 */
class GameEngine(
    val audio: GameAudio,
    val networkManager: NetworkManager? = null
) {
    var mode: GameMode = GameMode.BOT_DEATHMATCH
    var currentArena: MapArena = MapArena.SIMIEN_PEAKS

    // Game Entities
    val localPlayerId = "local_player"
    var localPlayer: PlayerState = PlayerState(
        id = localPlayerId,
        name = "Patriot",
        hero = WarriorHero.ARBEGNA,
        x = 500f,
        y = 500f,
        health = 100,
        maxHealth = 100,
        armor = 50,
        maxArmor = 50,
        currentWeapon = WeaponType.SPARROW_BOW,
        secondaryWeapon = WeaponType.WAR_SPEAR,
        ammoInMag = 12,
        reserveAmmo = 48,
        isBot = false
    )
    val players = CopyOnWriteArrayList<PlayerState>()
    val projectiles = CopyOnWriteArrayList<ProjectileEntity>()
    val pickups = CopyOnWriteArrayList<PickupEntity>()
    val particles = CopyOnWriteArrayList<ParticleEntity>()
    val damagePopups = CopyOnWriteArrayList<DamagePopup>()
    val killEvents = CopyOnWriteArrayList<KillEvent>()

    // Camera & Screen
    var cameraX = 0f
    var cameraY = 0f
    var cameraShake = 0f
    var viewWidth = 1920f
    var viewHeight = 1080f

    // Survival Mode Waves
    var currentWave = 1
    var waveEnemiesRemaining = 0
    var waveCountdownMs = 0L
    var waveBannerMessage = ""

    // Campaign Mode State
    var currentCampaignChapter: CampaignChapter? = null
    var currentChallenge: HeroicChallenge? = null
    var campaignTargetKills = 10
    var campaignStarsEarned = 0

    // Match State
    var matchTimerSec = 300f // 5 minutes countdown
    var matchEnded = false
    var winnerName = ""

    // Observable UI State Trigger
    private val _engineTick = MutableStateFlow(0L)
    val engineTick = _engineTick.asStateFlow()

    var onHapticFeedback: ((HapticType) -> Unit)? = null

    private var lastTickTime = System.currentTimeMillis()
    private var projectileIdCounter = 0L
    private var damagePopupIdCounter = 0L

    fun initGame(
        gameMode: GameMode,
        arena: MapArena,
        playerName: String,
        hero: WarriorHero,
        botCount: Int = 3,
        chapter: CampaignChapter? = null,
        challenge: HeroicChallenge? = null
    ) {
        try {
            mode = gameMode
            currentArena = arena
            currentCampaignChapter = chapter
            currentChallenge = challenge
            campaignStarsEarned = 0
            matchEnded = false
            winnerName = ""
            matchTimerSec = when {
                gameMode == GameMode.CAMPAIGN_MISSION && chapter != null -> chapter.timeLimitSec
                gameMode == GameMode.HEROIC_CHALLENGE && challenge != null -> challenge.timeLimitSec
                else -> 300f
            }
            campaignTargetKills = when {
                gameMode == GameMode.CAMPAIGN_MISSION && chapter != null -> chapter.targetKills
                gameMode == GameMode.HEROIC_CHALLENGE && challenge != null -> challenge.targetKills
                else -> 10
            }
            waveBannerMessage = when {
                gameMode == GameMode.CAMPAIGN_MISSION && chapter != null -> chapter.amharicTitle
                gameMode == GameMode.HEROIC_CHALLENGE && challenge != null -> challenge.amharicTitle
                else -> ""
            }
            currentWave = 1
            killEvents.clear()
            projectiles.clear()
            particles.clear()
            damagePopups.clear()
            players.clear()
            pickups.clear()

            val effectiveHero = when {
                gameMode == GameMode.CAMPAIGN_MISSION && chapter != null -> chapter.playerHero
                gameMode == GameMode.HEROIC_CHALLENGE && challenge != null -> challenge.playerHero
                else -> hero
            }
            val initialWep = if (gameMode == GameMode.HEROIC_CHALLENGE && challenge != null) challenge.lockedWeapon else effectiveHero.initialWeapon
            val extraShield = if (gameMode == GameMode.HEROIC_CHALLENGE && challenge != null) challenge.bonusShield else 0

            // Create Local Player
            val spawns = arena.getSpawnPoints()
            val pSpawn = spawns.firstOrNull() ?: SpawnPoint(500f, 500f)
            localPlayer = PlayerState(
                id = localPlayerId,
                name = playerName.ifBlank { "Patriot" },
                hero = effectiveHero,
                x = pSpawn.x,
                y = pSpawn.y,
                health = 100,
                maxHealth = 100,
                armor = 50 + effectiveHero.maxArmorBonus + extraShield,
                maxArmor = 50 + effectiveHero.maxArmorBonus + extraShield,
                currentWeapon = initialWep,
                secondaryWeapon = if (initialWep == WeaponType.SPARROW_BOW) WeaponType.WAR_SPEAR else initialWep,
                ammoInMag = initialWep.magazineSize,
                reserveAmmo = initialWep.magazineSize * 4,
                isBot = false,
                customization = WarriorCustomizationRepository.currentCustomization.copy()
            )
            players.add(localPlayer)

            // Initialize Pickups across the arena
            val pickupSpawns = arena.getPickupSpawns()
            var pId = 1
            val pickupTypes = listOf(
                PickupType.HEALTH_TENADAM,
                PickupType.SHIELD_GASHAA,
                PickupType.AMMO_CRATE,
                PickupType.SUPER_JETPACK,
                PickupType.WEAPON_DROP,
                PickupType.WEAPON_DROP
            )
            val sampleWeapons = listOf(
                WeaponType.MARTINI_HENRY,
                WeaponType.DOUBLE_SHOTGUN,
                WeaponType.AK47_KALASHNIKOV,
                WeaponType.SNIPER_DRAGUNOV,
                WeaponType.RPG7_ROCKET,
                WeaponType.PLASMA_RIFLE
            )
            pickupSpawns.forEachIndexed { index, spawn ->
                val pType = pickupTypes[index % pickupTypes.size]
                val wType = if (pType == PickupType.WEAPON_DROP) sampleWeapons[index % sampleWeapons.size] else null
                pickups.add(
                    PickupEntity(
                        id = pId++,
                        type = pType,
                        weaponType = wType,
                        x = spawn.x,
                        y = spawn.y
                    )
                )
            }

            // Initialize Bots or LAN players
            when (mode) {
                GameMode.BOT_DEATHMATCH -> {
                    val botHeroes = WarriorHero.values()
                    val botNames = listOf("Abebe (አበበ)", "Kebede (ከበደ)", "Almaz (አልማዝ)", "Taye (ታየ)", "Mulu (ሙሉ)")
                    for (i in 0 until botCount) {
                        val bSpawn = spawns[(i + 1) % spawns.size]
                        val bHero = botHeroes[(i + 1) % botHeroes.size]
                        val bot = PlayerState(
                            id = "bot_$i",
                            name = botNames.getOrElse(i) { "Bot ${i + 1}" },
                            hero = bHero,
                            x = bSpawn.x,
                            y = bSpawn.y,
                            health = 100,
                            maxHealth = 100,
                            armor = 40 + bHero.maxArmorBonus,
                            maxArmor = 40 + bHero.maxArmorBonus,
                            currentWeapon = bHero.initialWeapon,
                            secondaryWeapon = WeaponType.WAR_SPEAR,
                            ammoInMag = bHero.initialWeapon.magazineSize,
                            reserveAmmo = 999,
                            isBot = true,
                            customization = WarriorCustomizationRepository.presets[i % WarriorCustomizationRepository.presets.size].let { p ->
                                WarriorCustomization(cape = p.cape, kumeta = p.kumeta, headgear = p.headgear, sash = p.sash)
                            }
                        )
                        players.add(bot)
                    }
                }
                GameMode.CAMPAIGN_MISSION -> {
                    val enemyHero = chapter?.enemyHero ?: WarriorHero.COMMANDO
                    val enemyNames = listOf("Invader Vanguard", "Colonial Gunner", "Border Scout", "Assault Officer", "Elite Legion")
                    val count = 4
                    for (i in 0 until count) {
                        val bSpawn = spawns[(i + 1) % spawns.size]
                        val bot = PlayerState(
                            id = "campaign_bot_$i",
                            name = enemyNames.getOrElse(i) { "Enemy $i" },
                            hero = enemyHero,
                            x = bSpawn.x,
                            y = bSpawn.y,
                            health = 100,
                            maxHealth = 100,
                            armor = 40,
                            maxArmor = 40,
                            currentWeapon = when (chapter?.id ?: 1) {
                                1 -> if (i % 2 == 0) WeaponType.SPARROW_BOW else WeaponType.WAR_SPEAR
                                2 -> if (i % 2 == 0) WeaponType.FLINTLOCK_MUSKET else WeaponType.WAR_SPEAR
                                3 -> if (i % 2 == 0) WeaponType.MARTINI_HENRY else WeaponType.DOUBLE_SHOTGUN
                                else -> if (i % 2 == 0) WeaponType.AK47_KALASHNIKOV else WeaponType.RPG7_ROCKET
                            },
                            secondaryWeapon = WeaponType.WAR_SPEAR,
                            ammoInMag = 12,
                            reserveAmmo = 999,
                            isBot = true
                        )
                        players.add(bot)
                    }
                }
                GameMode.HEROIC_CHALLENGE -> {
                    val cHero = challenge?.playerHero ?: WarriorHero.COMMANDO
                    val count = 4
                    for (i in 0 until count) {
                        val bSpawn = spawns[(i + 1) % spawns.size]
                        val botWep = when (challenge?.id ?: 1) {
                            1 -> if (i % 2 == 0) WeaponType.WAR_SPEAR else WeaponType.SHOTEL_BLADE
                            2 -> WeaponType.SNIPER_DRAGUNOV
                            3 -> WeaponType.RPG7_ROCKET
                            else -> if (i % 2 == 0) WeaponType.AK47_KALASHNIKOV else WeaponType.DOUBLE_SHOTGUN
                        }
                        val bot = PlayerState(
                            id = "challenge_bot_$i",
                            name = "Rival Warrior ${i + 1}",
                            hero = cHero,
                            x = bSpawn.x,
                            y = bSpawn.y,
                            health = 100,
                            maxHealth = 100,
                            armor = 40,
                            maxArmor = 40,
                            currentWeapon = botWep,
                            secondaryWeapon = WeaponType.WAR_SPEAR,
                            ammoInMag = botWep.magazineSize,
                            reserveAmmo = 999,
                            isBot = true
                        )
                        players.add(bot)
                    }
                }
                GameMode.SURVIVAL_WAVES -> {
                    startSurvivalWave(1)
                }
                GameMode.LAN_MULTIPLAYER -> {
                    // Remote players will be populated from NetworkManager
                    syncLanLobbyPlayers()
                }
                GameMode.WEAPON_RANGE -> {
                    // Dummy target bot for target practice
                    val dummy = PlayerState(
                        id = "target_dummy",
                        name = "Training Target (ልምምድ)",
                        hero = WarriorHero.COMMANDO,
                        x = pSpawn.x + 500f,
                        y = pSpawn.y,
                        health = 9999,
                        maxHealth = 9999,
                        armor = 9999,
                        maxArmor = 9999,
                        currentWeapon = WeaponType.SPARROW_BOW,
                        isBot = true
                    )
                    players.add(dummy)
                }
            }

            cameraX = localPlayer.x
            cameraY = localPlayer.y
            lastTickTime = System.currentTimeMillis()
        } catch (t: Throwable) {
            android.util.Log.e("GameEngine", "Error initializing game", t)
        }
    }

    private fun startSurvivalWave(wave: Int) {
        currentWave = wave
        waveCountdownMs = 2500L
        val enemyCount = 3 + wave * 2
        waveEnemiesRemaining = enemyCount
        val spawns = currentArena.getSpawnPoints()

        // Remove old dead bots
        players.removeAll { it.isBot }

        for (i in 0 until enemyCount) {
            val bSpawn = spawns[(i + 1) % spawns.size]
            val waveWeapon = when {
                wave == 1 -> WeaponType.SPARROW_BOW
                wave == 2 -> if (i % 2 == 0) WeaponType.WAR_SPEAR else WeaponType.FLINTLOCK_MUSKET
                wave == 3 -> if (i % 2 == 0) WeaponType.DOUBLE_SHOTGUN else WeaponType.MARTINI_HENRY
                wave == 4 -> if (i % 2 == 0) WeaponType.AK47_KALASHNIKOV else WeaponType.SNIPER_DRAGUNOV
                else -> if (i == 0) WeaponType.RPG7_ROCKET else WeaponType.PLASMA_RIFLE
            }

            val invader = PlayerState(
                id = "invader_${wave}_$i",
                name = "Invader W$wave-${i + 1}",
                hero = if (i == 0 && wave >= 3) WarriorHero.MEHAL_SEFARI else WarriorHero.ARBEGNA,
                x = bSpawn.x + Random.nextFloat() * 100f - 50f,
                y = bSpawn.y,
                health = 70 + wave * 15,
                maxHealth = 70 + wave * 15,
                armor = 20 + wave * 10,
                maxArmor = 20 + wave * 10,
                currentWeapon = waveWeapon,
                ammoInMag = waveWeapon.magazineSize,
                reserveAmmo = 999,
                isBot = true
            )
            players.add(invader)
        }
    }

    fun syncLanLobbyPlayers() {
        val net = networkManager ?: return
        net.lobbyPlayers.forEachIndexed { index, lp ->
            if (lp.id != "host" || !net.isHost) {
                if (players.none { it.id == lp.id }) {
                    val spawns = currentArena.getSpawnPoints()
                    val sp = spawns[(index + 1) % spawns.size]
                    val remoteP = PlayerState(
                        id = lp.id,
                        name = lp.name,
                        hero = lp.hero,
                        x = sp.x,
                        y = sp.y,
                        health = 100,
                        maxHealth = 100,
                        armor = 50 + lp.hero.maxArmorBonus,
                        maxArmor = 50 + lp.hero.maxArmorBonus,
                        currentWeapon = lp.hero.initialWeapon,
                        isBot = false
                    )
                    players.add(remoteP)
                }
            }
        }
    }

    var inputMoveX = 0f
    var inputMoveY = 0f
    var inputAimX = 0f
    var inputAimY = 0f
    var isFireHeld = false
    var isJetpackHeld = false

    /**
     * Main update tick called ~60 times per second from Composable LaunchedEffect.
     */
    fun update(
        moveX: Float = inputMoveX, // -1f (left) to +1f (right)
        moveY: Float = inputMoveY, // -1f (up/jetpack) to +1f (down)
        aimX: Float = inputAimX,
        aimY: Float = inputAimY,
        isFireHeld: Boolean = this.isFireHeld,
        isJetpackHeld: Boolean = this.isJetpackHeld
    ) {
        val now = System.currentTimeMillis()
        try {
            val dt = ((now - lastTickTime).coerceIn(8L, 33L)).toFloat() / 1000f
            lastTickTime = now

            if (matchEnded) {
                _engineTick.value = now
                return
            }

            // 1. Update Local Player Controls & Physics
            if (!localPlayer.isDead) {
                val effectiveMoveY = if (isJetpackHeld) -1.0f else moveY
                val aimMagnitude = hypot(aimX, aimY)
                val shouldFire = isFireHeld || (aimMagnitude > 0.35f)

                if (mode == GameMode.HEROIC_CHALLENGE && currentChallenge?.hasInfiniteJetpack == true) {
                    localPlayer.jetpackFuel = localPlayer.maxJetpackFuel
                }
                updatePlayerPhysics(localPlayer, moveX, effectiveMoveY, aimX, aimY, dt, isFiring = shouldFire)
                if (mode == GameMode.HEROIC_CHALLENGE && currentChallenge?.hasInfiniteJetpack == true) {
                    localPlayer.jetpackFuel = localPlayer.maxJetpackFuel
                }
                if (shouldFire) {
                    tryFireWeapon(localPlayer)
                }
            } else {
                // Respawn countdown
                localPlayer.respawnTimerMs -= (dt * 1000f).toLong()
                if (localPlayer.respawnTimerMs <= 0) {
                    respawnPlayer(localPlayer)
                }
            }

            // 2. Update AI Bots
            if (mode == GameMode.BOT_DEATHMATCH || mode == GameMode.CAMPAIGN_MISSION || mode == GameMode.HEROIC_CHALLENGE || mode == GameMode.SURVIVAL_WAVES || mode == GameMode.WEAPON_RANGE) {
                players.filter { it.isBot && !it.isDead }.forEach { bot ->
                    updateBotAI(bot, dt)
                }
                players.filter { it.isBot && it.isDead }.forEach { bot ->
                    bot.respawnTimerMs -= (dt * 1000f).toLong()
                    if (bot.respawnTimerMs <= 0 && mode != GameMode.SURVIVAL_WAVES) {
                        respawnPlayer(bot)
                    }
                }
            }

            // 3. Process LAN incoming packets
            if (mode == GameMode.LAN_MULTIPLAYER && networkManager != null) {
                processLanPackets()
            }

            // 4. Update Projectiles
            updateProjectiles(dt)

            // 5. Update Pickups
            updatePickups(dt)

            // 6. Update Particles
            updateParticles(dt)

            // 7. Update Damage Popups
            val toRemovePopups = mutableListOf<DamagePopup>()
            for (pop in damagePopups) {
                pop.y -= 30f * dt
                pop.lifeTimeMs -= (dt * 1000f).toLong()
                if (pop.lifeTimeMs <= 0) {
                    toRemovePopups.add(pop)
                }
            }
            if (toRemovePopups.isNotEmpty()) {
                damagePopups.removeAll(toRemovePopups)
            }

            // 8. Update Camera Follow
            val targetCamX = if (localPlayer.x.isNaN()) 500f else localPlayer.x
            val targetCamY = if (localPlayer.y.isNaN()) 500f else localPlayer.y
            cameraX += (targetCamX - cameraX) * 0.12f
            cameraY += (targetCamY - cameraY) * 0.12f

            val safeViewW = if (viewWidth.isNaN() || viewWidth <= 0f) 1920f else viewWidth
            val safeViewH = if (viewHeight.isNaN() || viewHeight <= 0f) 1080f else viewHeight

            val minCamX = safeViewW / 2f
            val maxCamX = currentArena.worldWidth - safeViewW / 2f
            cameraX = if (minCamX <= maxCamX) {
                cameraX.coerceIn(minCamX, maxCamX)
            } else {
                currentArena.worldWidth / 2f
            }

            val minCamY = safeViewH / 2f
            val maxCamY = currentArena.worldHeight - safeViewH / 2f
            cameraY = if (minCamY <= maxCamY) {
                cameraY.coerceIn(minCamY, maxCamY)
            } else {
                currentArena.worldHeight / 2f
            }

            if (cameraShake > 0) {
                cameraShake -= dt * 15f
                if (cameraShake < 0) cameraShake = 0f
            }

            // 9. Match timer countdown and win condition
            if (!matchEnded && mode != GameMode.WEAPON_RANGE) {
                matchTimerSec = (matchTimerSec - dt).coerceAtLeast(0f)
                if (matchTimerSec <= 0f) {
                    matchEnded = true
                    if (mode == GameMode.CAMPAIGN_MISSION) {
                        val localP = players.find { it.id == localPlayerId }
                        if (localP != null && localP.kills >= campaignTargetKills) {
                            winnerName = localP.name
                            val stars = when {
                                localP.health >= 70 -> 3
                                localP.health >= 40 -> 2
                                else -> 1
                            }
                            campaignStarsEarned = stars
                            currentCampaignChapter?.let { ch ->
                                CampaignRepository.markChapterCompleted(ch.id, stars)
                            }
                            audio.playSound("fanfare")
                        } else {
                            winnerName = "Defeat"
                        }
                    } else if (mode == GameMode.HEROIC_CHALLENGE) {
                        val localP = players.find { it.id == localPlayerId }
                        if (localP != null && localP.kills >= campaignTargetKills) {
                            winnerName = localP.name
                            val stars = when {
                                localP.health >= 60 -> 3
                                localP.health >= 30 -> 2
                                else -> 1
                            }
                            campaignStarsEarned = stars
                            currentChallenge?.let { ch ->
                                ChallengeRepository.markCompleted(ch.id, localP.kills, matchTimerSec)
                            }
                            audio.playSound("fanfare")
                        } else {
                            winnerName = "Defeat"
                        }
                    } else {
                        val topPlayer = players.maxByOrNull { it.kills }
                        winnerName = topPlayer?.name ?: "Draw"
                        audio.playSound("fanfare")
                    }
                }
            }

            // Wave survival transitions
            if (mode == GameMode.SURVIVAL_WAVES && !matchEnded) {
                val aliveInvaders = players.count { it.isBot && !it.isDead }
                if (aliveInvaders == 0) {
                    if (waveCountdownMs > 0L) {
                        waveCountdownMs -= (dt * 1000f).toLong()
                        if (waveCountdownMs <= 0L) {
                            waveBannerMessage = ""
                            startSurvivalWave(currentWave + 1)
                        }
                    } else {
                        audio.playSound("fanfare")
                        waveCountdownMs = 3500L
                        waveBannerMessage = "Wave $currentWave Cleared! Next wave incoming..."
                        // Drop bonus weapon & medkit at fortress center
                        pickups.add(
                            PickupEntity(
                                id = 2000 + currentWave,
                                type = PickupType.WEAPON_DROP,
                                weaponType = WeaponType.values().random(),
                                x = currentArena.worldWidth / 2f + (Random.nextFloat() - 0.5f) * 200f,
                                y = 350f
                            )
                        )
                        pickups.add(
                            PickupEntity(
                                id = 3000 + currentWave,
                                type = PickupType.HEALTH_TENADAM,
                                x = currentArena.worldWidth / 2f,
                                y = 350f
                            )
                        )
                    }
                }
            }

            // Broadcast local state on LAN
            if (mode == GameMode.LAN_MULTIPLAYER && networkManager != null) {
                networkManager.broadcastPlayerSync(
                    RemotePlayerPacket(
                        id = localPlayer.id,
                        name = localPlayer.name,
                        hero = localPlayer.hero,
                        x = localPlayer.x,
                        y = localPlayer.y,
                        vx = localPlayer.vx,
                        vy = localPlayer.vy,
                        aimAngleDeg = localPlayer.aimAngleDeg,
                        isFacingRight = localPlayer.isFacingRight,
                        isJetpacking = localPlayer.isJetpacking,
                        currentWeapon = localPlayer.currentWeapon,
                        health = localPlayer.health,
                        armor = localPlayer.armor,
                        isDead = localPlayer.isDead,
                        kills = localPlayer.kills,
                        deaths = localPlayer.deaths
                    )
                )
            }
        } catch (t: Throwable) {
            android.util.Log.e("GameEngine", "Error during game update tick", t)
        }

        _engineTick.value = now
    }

    private fun updatePlayerPhysics(
        player: PlayerState,
        moveX: Float,
        moveY: Float,
        aimX: Float,
        aimY: Float,
        dt: Float,
        isFiring: Boolean = false
    ) {
        // Reload timer
        if (player.isReloading) {
            player.reloadTimerMs -= (dt * 1000f).toLong()
            if (player.reloadTimerMs <= 0) {
                player.isReloading = false
                val needed = player.currentWeapon.magazineSize - player.ammoInMag
                val toAdd = min(needed, player.reserveAmmo)
                player.ammoInMag += toAdd
                if (player.reserveAmmo < 900) player.reserveAmmo -= toAdd
            }
        }

        // Invulnerability decay
        if (player.isInvulnerableMs > 0) {
            player.isInvulnerableMs -= (dt * 1000f).toLong()
        }

        // Melee cooldown decay
        if (player.meleeCooldownMs > 0) {
            player.meleeCooldownMs -= (dt * 1000f).toLong()
        }

        // Aiming orientation
        if (abs(aimX) > 0.1f || abs(aimY) > 0.1f) {
            player.aimAngleDeg = (atan2(aimY, aimX) * (180.0 / PI)).toFloat()
            player.isFacingRight = abs(player.aimAngleDeg) < 90f
        } else if (abs(moveX) > 0.1f) {
            player.isFacingRight = moveX > 0f
            player.aimAngleDeg = if (player.isFacingRight) 0f else 180f
        }

        // Soft aim assist for local player
        if (player.id == localPlayerId) {
            applySoftAimAssist(player, aimX, aimY, isFiring)
        }

        // Physics, gravity, jetpack flight, and collision detection
        val platforms = currentArena.getPlatforms()
        PhysicsEngine.step(
            player = player,
            moveX = moveX,
            moveY = moveY,
            dt = dt,
            platforms = platforms,
            worldWidth = currentArena.worldWidth,
            worldHeight = currentArena.worldHeight,
            onJetpackThrust = { intensity ->
                spawnJetpackParticles(player, intensity)
                if (Random.nextInt(10) == 0 && player.id == localPlayerId) {
                    audio.playSound("jetpack")
                }
            }
        )
    }

    private fun applySoftAimAssist(player: PlayerState, aimX: Float, aimY: Float, isFiring: Boolean) {
        val hasActiveAim = abs(aimX) > 0.1f || abs(aimY) > 0.1f
        val enemies = players.filter { it.id != player.id && !it.isDead }
        if (enemies.isEmpty()) return

        val maxAssistRange = 650f

        if (hasActiveAim) {
            val inputAngleDeg = (atan2(aimY, aimX) * (180.0 / PI)).toFloat()
            var bestTarget: PlayerState? = null
            var bestScore = Float.MAX_VALUE

            for (enemy in enemies) {
                val dx = enemy.x - player.x
                val dy = enemy.y - player.y
                val dist = hypot(dx, dy)
                if (dist < maxAssistRange) {
                    val targetAngleDeg = (atan2(dy, dx) * (180.0 / PI)).toFloat()
                    var angleDiff = abs(targetAngleDeg - inputAngleDeg)
                    if (angleDiff > 180f) angleDiff = 360f - angleDiff

                    if (angleDiff <= 48f) {
                        val score = angleDiff * 2f + dist * 0.1f
                        if (score < bestScore) {
                            bestScore = score
                            bestTarget = enemy
                        }
                    }
                }
            }

            if (bestTarget != null) {
                val dx = bestTarget.x - player.x
                val dy = bestTarget.y - player.y
                val targetAngleDeg = (atan2(dy, dx) * (180.0 / PI)).toFloat()
                var angleDiff = ((targetAngleDeg - inputAngleDeg + 180f) % 360f + 360f) % 360f - 180f
                if (!angleDiff.isNaN()) {
                    player.aimAngleDeg = inputAngleDeg + angleDiff * 0.55f
                    player.isFacingRight = abs(player.aimAngleDeg) < 90f
                }
            } else {
                player.aimAngleDeg = inputAngleDeg
                player.isFacingRight = abs(player.aimAngleDeg) < 90f
            }
        } else if (isFiring) {
            val nearest = enemies.minByOrNull { hypot(it.x - player.x, it.y - player.y) }
            if (nearest != null) {
                val dist = hypot(nearest.x - player.x, nearest.y - player.y)
                if (dist < maxAssistRange) {
                    val targetAngleDeg = (atan2(nearest.y - player.y, nearest.x - player.x) * (180.0 / PI)).toFloat()
                    player.aimAngleDeg = targetAngleDeg
                    player.isFacingRight = abs(targetAngleDeg) < 90f
                }
            }
        }
    }

    private fun updateBotAI(bot: PlayerState, dt: Float) {
        // Target nearest alive enemy
        val target = players.filter { it.id != bot.id && !it.isDead }
            .minByOrNull { hypot(it.x - bot.x, it.y - bot.y) }

        if (target == null) return

        val dx = target.x - bot.x
        val dy = target.y - bot.y
        val dist = hypot(dx, dy)

        // Aim towards target with slight inaccuracy
        val targetAngle = (atan2(dy, dx) * (180.0 / PI)).toFloat()
        bot.aimAngleDeg = targetAngle
        bot.isFacingRight = abs(targetAngle) < 90f

        var botMoveX = 0f
        var botMoveY = 0f

        // If target is far, move closer; if too close, back away
        val preferredDist = if (bot.currentWeapon == WeaponType.SHOTEL_BLADE) 50f else 400f
        if (dist > preferredDist + 50f) {
            botMoveX = if (dx > 0) 0.8f else -0.8f
        } else if (dist < preferredDist - 100f && bot.currentWeapon != WeaponType.SHOTEL_BLADE) {
            botMoveX = if (dx > 0) -0.6f else 0.6f
        }

        // If target is higher, engage jetpack
        if (dy < -80f && bot.jetpackFuel > 25f) {
            botMoveY = -0.8f
        }

        // If low health, check if a health pickup is nearby
        if (bot.health < 40) {
            val med = pickups.filter { it.type == PickupType.HEALTH_TENADAM && it.isAvailable }
                .minByOrNull { hypot(it.x - bot.x, it.y - bot.y) }
            if (med != null) {
                botMoveX = if (med.x > bot.x) 0.9f else -0.9f
                if (med.y < bot.y - 40f) botMoveY = -0.9f
            }
        }

        updatePlayerPhysics(bot, botMoveX, botMoveY, dx, dy, dt)

        // Bot Shoot condition
        if (dist < 800f && abs(bot.aimAngleDeg - targetAngle) < 30f) {
            tryFireWeapon(bot)
        }
    }

    fun tryFireWeapon(player: PlayerState) {
        val now = System.currentTimeMillis()
        if (now < player.nextFireAllowedMs || player.isDead || player.isReloading) return

        if (player.ammoInMag <= 0) {
            startReload(player)
            return
        }

        val weapon = player.currentWeapon
        player.ammoInMag--
        player.nextFireAllowedMs = now + weapon.fireRateMs

        // Play procedural audio
        audio.playSound(weapon.soundKey)

        val rad = player.aimAngleDeg * (PI / 180.0).toFloat()
        val spawnDist = 35f
        val startX = player.x + cos(rad) * spawnDist
        val startY = player.y + sin(rad) * spawnDist

        // Melee special: Dash forward with Shotel slash!
        if (weapon == WeaponType.SHOTEL_BLADE) {
            player.vx += cos(rad) * 450f
            player.vy += sin(rad) * 200f
            spawnSlashShockwave(player, rad)
            if (player.id == localPlayerId) {
                onHapticFeedback?.invoke(HapticType.LIGHT_FIRE)
            }
            return
        }

        // Multi-pellet (Shotgun) or Single Bullet / Arrow / Rocket
        val pellets = weapon.pellets
        val spreadHalf = weapon.spreadAngleDeg / 2f

        for (i in 0 until pellets) {
            val spreadOffset = if (pellets > 1) {
                Random.nextFloat() * weapon.spreadAngleDeg - spreadHalf
            } else {
                (Random.nextFloat() * 2f - 1f) * weapon.spreadAngleDeg
            }
            val pelletRad = (player.aimAngleDeg + spreadOffset) * (PI / 180.0).toFloat()
            val speed = weapon.projectileSpeed * 40f
            val vx = cos(pelletRad) * speed
            val vy = sin(pelletRad) * speed

            val proj = ProjectileEntity(
                id = ++projectileIdCounter,
                shooterId = player.id,
                weaponType = weapon,
                x = startX,
                y = startY,
                vx = vx,
                vy = vy,
                gravity = weapon.gravityEffect * 600f,
                damage = weapon.damage + if (weapon == WeaponType.SHOTEL_BLADE) player.hero.meleeDamageBonus else 0,
                radius = if (weapon.isExplosive) 8f else if (weapon == WeaponType.SPARROW_BOW) 5f else 4f,
                lifeTimeMs = 0L,
                maxLifeTimeMs = if (weapon == WeaponType.SPARROW_BOW) 3000L else 2200L,
                isExplosive = weapon.isExplosive,
                blastRadius = weapon.blastRadius,
                color = weapon.color
            )
            projectiles.add(proj)
        }

        // Recoil pushback
        player.vx -= cos(rad) * (weapon.damage * 0.7f)
        if (weapon.isExplosive) {
            cameraShake = 12f
        }

        // Haptic feedback for local player
        if (player.id == localPlayerId) {
            val haptic = if (weapon.isExplosive) {
                HapticType.EXPLOSION
            } else if (weapon.damage >= 45) {
                HapticType.HEAVY_FIRE
            } else {
                HapticType.LIGHT_FIRE
            }
            onHapticFeedback?.invoke(haptic)
        }

        // LAN broadcast
        if (mode == GameMode.LAN_MULTIPLAYER && player.id == localPlayerId && networkManager != null) {
            networkManager.broadcastFire(
                NetworkFireEvent(player.id, weapon, startX, startY, player.aimAngleDeg)
            )
        }
    }

    private fun spawnSlashShockwave(player: PlayerState, rad: Float) {
        val slashX = player.x + cos(rad) * 45f
        val slashY = player.y + sin(rad) * 45f
        val slashRadius = 60f

        // Slash impact particles
        for (i in 0..12) {
            val pRad = rad + (Random.nextFloat() - 0.5f) * 1.5f
            val pSpeed = Random.nextFloat() * 200f + 50f
            particles.add(
                ParticleEntity(
                    x = slashX,
                    y = slashY,
                    vx = cos(pRad) * pSpeed,
                    vy = sin(pRad) * pSpeed,
                    color = Color(0xFFEF4444),
                    radius = 4f,
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = 250L
                )
            )
        }

        // Damage enemies in slash arc
        players.filter { it.id != player.id && !it.isDead }.forEach { target ->
            val dist = hypot(target.x - slashX, target.y - slashY)
            if (dist < slashRadius) {
                applyDamage(target, 95 + player.hero.meleeDamageBonus, player, WeaponType.SHOTEL_BLADE)
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val platforms = currentArena.getPlatforms()
        val toRemove = mutableListOf<ProjectileEntity>()

        for (p in projectiles) {
            p.vy += p.gravity * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.lifeTimeMs += (dt * 1000f).toLong()

            // Trail particles for rockets or plasma
            if (p.isExplosive && Random.nextInt(3) == 0) {
                particles.add(
                    ParticleEntity(
                        x = p.x,
                        y = p.y,
                        vx = (Random.nextFloat() - 0.5f) * 40f,
                        vy = (Random.nextFloat() - 0.5f) * 40f,
                        color = Color(0xFFFF6D00),
                        radius = 6f,
                        lifeTimeMs = 0L,
                        maxLifeTimeMs = 400L
                    )
                )
            }

            var remove = false

            // Hit platforms
            for (plat in platforms) {
                if (plat.bounds.contains(androidx.compose.ui.geometry.Offset(p.x, p.y))) {
                    remove = true
                    if (p.isExplosive) {
                        triggerExplosion(p.x, p.y, p.blastRadius, p.damage, p.shooterId)
                    } else {
                        // Impact sparks
                        spawnImpactSparks(p.x, p.y, p.color)
                    }
                    break
                }
            }

            // Hit players
            if (!remove) {
                for (victim in players) {
                    if (victim.id != p.shooterId && !victim.isDead && victim.isInvulnerableMs <= 0) {
                        val hitDist = hypot(victim.x - p.x, victim.y - p.y)
                        if (hitDist < 30f + p.radius) {
                            remove = true
                            val shooter = players.find { it.id == p.shooterId }
                            if (p.isExplosive) {
                                triggerExplosion(p.x, p.y, p.blastRadius, p.damage, p.shooterId)
                            } else {
                                applyDamage(victim, p.damage, shooter, p.weaponType)
                                spawnBloodOrSpark(p.x, p.y)
                            }
                            break
                        }
                    }
                }
            }

            // Out of bounds or lifetime expired
            if (!remove && (p.x < 0 || p.x > currentArena.worldWidth || p.y < 0 || p.y > currentArena.worldHeight || p.lifeTimeMs >= p.maxLifeTimeMs)) {
                remove = true
                if (p.isExplosive && p.lifeTimeMs >= p.maxLifeTimeMs) {
                    triggerExplosion(p.x, p.y, p.blastRadius, p.damage, p.shooterId)
                }
            }

            if (remove) {
                toRemove.add(p)
            }
        }

        if (toRemove.isNotEmpty()) {
            projectiles.removeAll(toRemove)
        }
    }

    private fun triggerExplosion(x: Float, y: Float, radius: Float, maxDamage: Int, shooterId: String) {
        audio.playSound("explosion")
        cameraShake = 22f

        // Explosion particles (fire and smoke)
        for (i in 0..28) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 320f + 80f
            val col = if (i % 2 == 0) Color(0xFFFF6D00) else Color(0xFFFFD600)
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(ang) * speed,
                    vy = sin(ang) * speed,
                    color = col,
                    radius = Random.nextFloat() * 12f + 4f,
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = Random.nextLong(350L, 700L)
                )
            )
        }

        // Radial splash damage & airborne knockback
        val shooter = players.find { it.id == shooterId }
        players.filter { !it.isDead && it.isInvulnerableMs <= 0 }.forEach { victim ->
            val dist = hypot(victim.x - x, victim.y - y)
            if (dist < radius) {
                val falloff = (1f - dist / radius).coerceIn(0.2f, 1f)
                val dmg = (maxDamage * falloff).toInt()
                // Radial pushback impulse
                val angle = atan2(victim.y - y, victim.x - x)
                victim.vx += cos(angle) * 550f * falloff
                victim.vy += sin(angle) * 450f * falloff - 150f
                applyDamage(victim, dmg, shooter, WeaponType.RPG7_ROCKET)
            }
        }
    }

    fun applyDamage(victim: PlayerState, amount: Int, attacker: PlayerState?, weapon: WeaponType) {
        if (victim.isDead || victim.isInvulnerableMs > 0) return

        victim.lastDamagedById = attacker?.id ?: ""
        audio.playSound("hit")

        // Armor absorbs 65% of damage first
        var remainingDmg = amount
        if (victim.armor > 0) {
            val armorAbsorption = (amount * 0.65f).toInt()
            val actualArmorLoss = min(victim.armor, armorAbsorption)
            victim.armor -= actualArmorLoss
            remainingDmg = amount - actualArmorLoss
        }

        victim.health -= remainingDmg

        if (victim.id == localPlayerId) {
            onHapticFeedback?.invoke(HapticType.DAMAGE_TAKEN)
        }

        // Damage popup
        damagePopups.add(
            DamagePopup(
                id = ++damagePopupIdCounter,
                x = victim.x + (Random.nextFloat() - 0.5f) * 20f,
                y = victim.y - 40f,
                text = "-$amount",
                color = if (victim.armor > 0) Color(0xFF38BDF8) else Color(0xFFEF4444),
                lifeTimeMs = 600L
            )
        )

        // Player Death check
        if (victim.health <= 0) {
            victim.health = 0
            victim.isDead = true
            victim.deaths++
            victim.respawnTimerMs = 3000L

            attacker?.let {
                it.kills++
                if (it.kills >= 15 && mode == GameMode.BOT_DEATHMATCH) {
                    matchEnded = true
                    winnerName = it.name
                    audio.playSound("fanfare")
                } else if (mode == GameMode.CAMPAIGN_MISSION && it.id == localPlayerId && it.kills >= campaignTargetKills) {
                    matchEnded = true
                    winnerName = it.name
                    audio.playSound("fanfare")
                    val stars = when {
                        it.health >= 70 && matchTimerSec >= 60f -> 3
                        it.health >= 40 -> 2
                        else -> 1
                    }
                    campaignStarsEarned = stars
                    currentCampaignChapter?.let { ch ->
                        CampaignRepository.markChapterCompleted(ch.id, stars)
                    }
                } else if (mode == GameMode.HEROIC_CHALLENGE && it.id == localPlayerId && it.kills >= campaignTargetKills) {
                    matchEnded = true
                    winnerName = it.name
                    audio.playSound("fanfare")
                    val stars = when {
                        it.health >= 60 && matchTimerSec >= 40f -> 3
                        it.health >= 30 -> 2
                        else -> 1
                    }
                    campaignStarsEarned = stars
                    currentChallenge?.let { ch ->
                        ChallengeRepository.markCompleted(ch.id, it.kills, matchTimerSec)
                    }
                }
            }

            val killMsg = KillEvent(
                killerName = attacker?.name ?: "Environment",
                victimName = victim.name,
                weaponTitle = weapon.amharicName
            )
            killEvents.add(killMsg)

            // Spawn death explosion & weapon drop pickup
            spawnDeathParticles(victim.x, victim.y, victim.hero.primaryColor)
            pickups.add(
                PickupEntity(
                    id = 1000 + Random.nextInt(9000),
                    type = PickupType.WEAPON_DROP,
                    weaponType = victim.currentWeapon,
                    x = victim.x,
                    y = victim.y
                )
            )
        }
    }

    private fun respawnPlayer(player: PlayerState) {
        val spawns = currentArena.getSpawnPoints()
        val sp = spawns.random()
        player.x = sp.x
        player.y = sp.y
        player.vx = 0f
        player.vy = 0f
        player.health = player.maxHealth
        player.armor = player.maxArmor
        player.jetpackFuel = player.maxJetpackFuel
        player.isDead = false
        player.isInvulnerableMs = 2000L // 2 sec spawn protection
        player.ammoInMag = player.currentWeapon.magazineSize
    }

    fun startReload(player: PlayerState) {
        if (player.isReloading || player.ammoInMag == player.currentWeapon.magazineSize || player.reserveAmmo <= 0) return
        player.isReloading = true
        player.reloadTimerMs = player.currentWeapon.reloadTimeMs
    }

    fun switchWeapon(player: PlayerState) {
        val temp = player.currentWeapon
        player.currentWeapon = player.secondaryWeapon
        player.secondaryWeapon = temp
        player.isReloading = false
        player.ammoInMag = player.currentWeapon.magazineSize
    }

    fun equipRangeWeapon(weapon: WeaponType) {
        localPlayer.currentWeapon = weapon
        localPlayer.ammoInMag = weapon.magazineSize
        localPlayer.reserveAmmo = 999
        localPlayer.isReloading = false
        damagePopups.add(
            DamagePopup(
                id = ++damagePopupIdCounter,
                x = localPlayer.x,
                y = localPlayer.y - 40f,
                text = "Equipped ${weapon.amharicName}",
                color = weapon.color,
                lifeTimeMs = 700L
            )
        )
    }

    fun executeMeleeSlash(player: PlayerState) {
        if (player.isDead || player.meleeCooldownMs > 0) return
        player.meleeCooldownMs = 1200L
        audio.playSound("slash")
        val rad = player.aimAngleDeg * (PI / 180.0).toFloat()
        spawnSlashShockwave(player, rad)
    }

    private fun updatePickups(dt: Float) {
        val iterator = pickups.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (!p.isAvailable) {
                p.respawnTimerMs -= (dt * 1000f).toLong()
                if (p.respawnTimerMs <= 0) {
                    p.isAvailable = true
                }
            } else {
                // Check if any player stepped on pickup
                for (player in players) {
                    if (!player.isDead && hypot(player.x - p.x, player.y - p.y) < 42f) {
                        consumePickup(player, p)
                        break
                    }
                }
            }
        }
    }

    private fun consumePickup(player: PlayerState, pickup: PickupEntity) {
        pickup.isAvailable = false
        pickup.respawnTimerMs = 15000L
        audio.playSound("pickup")

        when (pickup.type) {
            PickupType.HEALTH_TENADAM -> {
                player.health = (player.health + 45).coerceAtMost(player.maxHealth)
                damagePopups.add(DamagePopup(++damagePopupIdCounter, player.x, player.y - 30f, "+45 HP", Color(0xFF10B981), 600L))
            }
            PickupType.SHIELD_GASHAA -> {
                player.armor = (player.armor + 50).coerceAtMost(player.maxArmor)
                damagePopups.add(DamagePopup(++damagePopupIdCounter, player.x, player.y - 30f, "+50 Shield", Color(0xFF38BDF8), 600L))
            }
            PickupType.AMMO_CRATE -> {
                player.reserveAmmo += player.currentWeapon.magazineSize * 3
                player.ammoInMag = player.currentWeapon.magazineSize
                damagePopups.add(DamagePopup(++damagePopupIdCounter, player.x, player.y - 30f, "+Ammo", Color(0xFFF59E0B), 600L))
            }
            PickupType.SUPER_JETPACK -> {
                player.jetpackFuel = player.maxJetpackFuel
                player.superJetpackTimerMs = 8000L // 8 seconds of infinite fuel turbo boost
                damagePopups.add(DamagePopup(++damagePopupIdCounter, player.x, player.y - 30f, "Turbo Jet (8s)!", Color(0xFFFF6D00), 700L))
            }
            PickupType.WEAPON_DROP -> {
                pickup.weaponType?.let { wType ->
                    player.secondaryWeapon = player.currentWeapon
                    player.currentWeapon = wType
                    player.ammoInMag = wType.magazineSize
                    player.reserveAmmo = wType.magazineSize * 3
                    player.isReloading = false
                    damagePopups.add(DamagePopup(++damagePopupIdCounter, player.x, player.y - 30f, wType.amharicName, wType.color, 800L))
                }
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val toRemove = mutableListOf<ParticleEntity>()
        for (part in particles) {
            part.x += part.vx * dt
            part.y += part.vy * dt
            part.lifeTimeMs += (dt * 1000f).toLong()
            if (part.lifeTimeMs >= part.maxLifeTimeMs) {
                toRemove.add(part)
            }
        }
        if (toRemove.isNotEmpty()) {
            particles.removeAll(toRemove)
        }
    }

    private fun spawnJetpackParticles(player: PlayerState, intensity: Float = 1.0f) {
        val bootOffset = 25f
        val hasTurbo = player.superJetpackTimerMs > 0L
        for (i in 0..1) {
            val side = if (i == 0) -8f else 8f
            val baseColor = if (hasTurbo) {
                if (Random.nextBoolean()) Color(0xFF38BDF8) else Color(0xFFFFFFFF)
            } else {
                if (Random.nextBoolean()) Color(0xFFFF6D00) else Color(0xFFFFD600)
            }
            particles.add(
                ParticleEntity(
                    x = player.x + side,
                    y = player.y + bootOffset,
                    vx = (Random.nextFloat() - 0.5f) * (60f * intensity),
                    vy = Random.nextFloat() * (160f * intensity) + 100f,
                    color = baseColor,
                    radius = (Random.nextFloat() * 5f + 3f) * (if (hasTurbo) 1.3f else 1.0f),
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = (280L * intensity).toLong().coerceAtLeast(120L)
                )
            )
        }
    }

    private fun spawnImpactSparks(x: Float, y: Float, color: Color) {
        for (i in 0..6) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val spd = Random.nextFloat() * 120f + 40f
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = color,
                    radius = 3f,
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = 200L
                )
            )
        }
    }

    private fun spawnBloodOrSpark(x: Float, y: Float) {
        for (i in 0..8) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val spd = Random.nextFloat() * 160f + 60f
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = Color(0xFFDC2626),
                    radius = 3.5f,
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = 300L
                )
            )
        }
    }

    private fun spawnDeathParticles(x: Float, y: Float, heroColor: Color) {
        for (i in 0..30) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val spd = Random.nextFloat() * 280f + 50f
            particles.add(
                ParticleEntity(
                    x = x,
                    y = y,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd - 60f,
                    color = if (i % 2 == 0) heroColor else Color(0xFFDC2626),
                    radius = Random.nextFloat() * 6f + 3f,
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = 600L
                )
            )
        }
    }

    private fun processLanPackets() {
        val net = networkManager ?: return

        // 1. Remote player updates
        net.remotePlayers.forEach { (id, packet) ->
            if (id != localPlayerId) {
                var rp = players.find { it.id == id }
                if (rp == null) {
                    rp = PlayerState(
                        id = id,
                        name = packet.name,
                        hero = packet.hero,
                        x = packet.x,
                        y = packet.y,
                        health = packet.health,
                        armor = packet.armor,
                        currentWeapon = packet.currentWeapon,
                        isBot = false
                    )
                    players.add(rp)
                }
                // Smooth interpolation
                rp.x += (packet.x - rp.x) * 0.4f
                rp.y += (packet.y - rp.y) * 0.4f
                rp.vx = packet.vx
                rp.vy = packet.vy
                rp.aimAngleDeg = packet.aimAngleDeg
                rp.isFacingRight = packet.isFacingRight
                rp.isJetpacking = packet.isJetpacking
                rp.currentWeapon = packet.currentWeapon
                rp.health = packet.health
                rp.armor = packet.armor
                rp.isDead = packet.isDead
                rp.kills = packet.kills
                rp.deaths = packet.deaths
                if (rp.isJetpacking) {
                    spawnJetpackParticles(rp)
                }
            }
        }

        // 2. Incoming fire events
        while (net.incomingFireEvents.isNotEmpty()) {
            val fireEvent = net.incomingFireEvents.removeAt(0)
            if (fireEvent.shooterId != localPlayerId) {
                audio.playSound(fireEvent.weaponType.soundKey)
                val rad = fireEvent.aimAngleDeg * (PI / 180.0).toFloat()
                val speed = fireEvent.weaponType.projectileSpeed * 40f
                val proj = ProjectileEntity(
                    id = ++projectileIdCounter,
                    shooterId = fireEvent.shooterId,
                    weaponType = fireEvent.weaponType,
                    x = fireEvent.x,
                    y = fireEvent.y,
                    vx = cos(rad) * speed,
                    vy = sin(rad) * speed,
                    gravity = fireEvent.weaponType.gravityEffect * 600f,
                    damage = fireEvent.weaponType.damage,
                    radius = if (fireEvent.weaponType.isExplosive) 8f else 4f,
                    lifeTimeMs = 0L,
                    maxLifeTimeMs = 2200L,
                    isExplosive = fireEvent.weaponType.isExplosive,
                    blastRadius = fireEvent.weaponType.blastRadius,
                    color = fireEvent.weaponType.color
                )
                projectiles.add(proj)
            }
        }
    }
}

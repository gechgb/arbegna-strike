package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.audio.GameAudio
import com.example.engine.GameEngine
import com.example.lan.NetworkManager
import com.example.model.GameMode
import com.example.model.MapArena
import com.example.model.WarriorHero
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class AppScreen {
    MAIN_MENU,
    HERO_BARRACKS,
    ARMORY,
    LAN_LOBBY,
    CAMPAIGN_SELECT,
    HEROIC_CHALLENGES,
    WARRIOR_CUSTOMIZATION,
    IN_GAME
}

class MainActivity : ComponentActivity() {
    private lateinit var audio: GameAudio
    private val networkManager = NetworkManager()
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep screen on during action combat
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        audio = GameAudio(applicationContext)
        gameEngine = GameEngine(audio = audio, networkManager = networkManager)

        val vibrator = getSystemService(android.os.Vibrator::class.java)
        gameEngine.onHapticFeedback = { type ->
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    when (type) {
                        com.example.model.HapticType.LIGHT_FIRE -> vibrator?.vibrate(android.os.VibrationEffect.createOneShot(18L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        com.example.model.HapticType.HEAVY_FIRE -> vibrator?.vibrate(android.os.VibrationEffect.createOneShot(35L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        com.example.model.HapticType.EXPLOSION -> vibrator?.vibrate(android.os.VibrationEffect.createOneShot(60L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        com.example.model.HapticType.DAMAGE_TAKEN -> vibrator?.vibrate(android.os.VibrationEffect.createOneShot(45L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    when (type) {
                        com.example.model.HapticType.LIGHT_FIRE -> vibrator?.vibrate(18L)
                        com.example.model.HapticType.HEAVY_FIRE -> vibrator?.vibrate(35L)
                        com.example.model.HapticType.EXPLOSION -> vibrator?.vibrate(60L)
                        com.example.model.HapticType.DAMAGE_TAKEN -> vibrator?.vibrate(45L)
                    }
                }
            } catch (_: Throwable) {}
        }

        // Callback when LAN Host starts match
        networkManager.onMatchStartedCallback = { arena ->
            runOnUiThread {
                gameEngine.initGame(
                    gameMode = GameMode.LAN_MULTIPLAYER,
                    arena = arena,
                    playerName = "Patriot",
                    hero = WarriorHero.ARBEGNA
                )
            }
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF090D16)
                ) { innerPadding ->
                    ArbegnaAppContent(
                        audio = audio,
                        networkManager = networkManager,
                        engine = gameEngine,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audio.release()
        networkManager.stopAll()
    }
}

@Composable
fun ArbegnaAppContent(
    audio: GameAudio,
    networkManager: NetworkManager,
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    var playerName by remember { mutableStateOf("Patriot (አርበኛ)") }
    var selectedHero by remember { mutableStateOf(WarriorHero.ARBEGNA) }
    var selectedArena by remember { mutableStateOf(MapArena.SIMIEN_PEAKS) }
    var isPaused by remember { mutableStateOf(false) }

    // Control inputs
    var moveX by remember { mutableFloatStateOf(0f) }
    var moveY by remember { mutableFloatStateOf(0f) }
    var aimX by remember { mutableFloatStateOf(0f) }
    var aimY by remember { mutableFloatStateOf(0f) }
    var isFireHeld by remember { mutableStateOf(false) }
    var isJetpackHeld by remember { mutableStateOf(false) }

    fun resetInputs() {
        moveX = 0f
        moveY = 0f
        aimX = 0f
        aimY = 0f
        isFireHeld = false
        isJetpackHeld = false
    }

    // Listen to LAN match start callback
    LaunchedEffect(networkManager) {
        networkManager.onMatchStartedCallback = { arena ->
            resetInputs()
            engine.initGame(
                gameMode = GameMode.LAN_MULTIPLAYER,
                arena = arena,
                playerName = playerName,
                hero = selectedHero
            )
            currentScreen = AppScreen.IN_GAME
            isPaused = false
        }
    }

    // Main 60 FPS Game Physics & Tick Loop when IN_GAME
    if (currentScreen == AppScreen.IN_GAME && !isPaused) {
        LaunchedEffect(currentScreen, isPaused) {
            while (isActive) {
                try {
                    val effMoveY = if (isJetpackHeld) -1.0f else moveY
                    engine.update(
                        moveX = moveX,
                        moveY = effMoveY,
                        aimX = aimX,
                        aimY = aimY,
                        isFireHeld = isFireHeld,
                        isJetpackHeld = isJetpackHeld
                    )
                } catch (e: Throwable) {
                    android.util.Log.e("GameLoop", "Error during game update tick", e)
                }
                delay(16L) // ~60 ticks per sec
            }
        }
    }

    // Collect tick to trigger Canvas recomposition
    val tick by engine.engineTick.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF090D16))) {
        when (currentScreen) {
            AppScreen.MAIN_MENU -> {
                MainMenuScreen(
                    playerName = playerName,
                    onPlayerNameChange = { playerName = it },
                    selectedHero = selectedHero,
                    selectedArena = selectedArena,
                    onHeroClick = { currentScreen = AppScreen.HERO_BARRACKS },
                    onArmoryClick = { currentScreen = AppScreen.ARMORY },
                    onArenaSelect = { selectedArena = it },
                    onStartBotMatch = { botCount ->
                        try {
                            resetInputs()
                            engine.initGame(
                                gameMode = GameMode.BOT_DEATHMATCH,
                                arena = selectedArena,
                                playerName = playerName,
                                hero = selectedHero,
                                botCount = botCount
                            )
                            currentScreen = AppScreen.IN_GAME
                            isPaused = false
                        } catch (t: Throwable) {
                            android.util.Log.e("MainActivity", "Error starting bot match", t)
                        }
                    },
                    onStartSurvival = {
                        try {
                            resetInputs()
                            engine.initGame(
                                gameMode = GameMode.SURVIVAL_WAVES,
                                arena = selectedArena,
                                playerName = playerName,
                                hero = selectedHero
                            )
                            currentScreen = AppScreen.IN_GAME
                            isPaused = false
                        } catch (t: Throwable) {
                            android.util.Log.e("MainActivity", "Error starting survival match", t)
                        }
                    },
                    onLanMultiplayerClick = {
                        currentScreen = AppScreen.LAN_LOBBY
                    },
                    onPracticeRangeClick = {
                        try {
                            resetInputs()
                            engine.initGame(
                                gameMode = GameMode.WEAPON_RANGE,
                                arena = selectedArena,
                                playerName = playerName,
                                hero = selectedHero
                            )
                            currentScreen = AppScreen.IN_GAME
                            isPaused = false
                        } catch (t: Throwable) {
                            android.util.Log.e("MainActivity", "Error starting practice range", t)
                        }
                    },
                    onCampaignClick = {
                        currentScreen = AppScreen.CAMPAIGN_SELECT
                    },
                    onChallengesClick = {
                        currentScreen = AppScreen.HEROIC_CHALLENGES
                    },
                    onCustomizationClick = {
                        currentScreen = AppScreen.WARRIOR_CUSTOMIZATION
                    }
                )
            }

            AppScreen.HERO_BARRACKS -> {
                BackHandler { currentScreen = AppScreen.MAIN_MENU }
                HeroBarracksScreen(
                    currentHero = selectedHero,
                    onSelectHero = {
                        selectedHero = it
                        currentScreen = AppScreen.MAIN_MENU
                    },
                    onBackClick = { currentScreen = AppScreen.MAIN_MENU },
                    onCustomizationClick = { currentScreen = AppScreen.WARRIOR_CUSTOMIZATION }
                )
            }

            AppScreen.ARMORY -> {
                BackHandler { currentScreen = AppScreen.MAIN_MENU }
                ArmoryScreen(
                    onBackClick = { currentScreen = AppScreen.MAIN_MENU }
                )
            }

            AppScreen.LAN_LOBBY -> {
                BackHandler {
                    networkManager.stopAll()
                    currentScreen = AppScreen.MAIN_MENU
                }
                LanMultiplayerScreen(
                    networkManager = networkManager,
                    playerName = playerName,
                    selectedHero = selectedHero,
                    selectedArena = selectedArena,
                    onArenaSelect = { selectedArena = it },
                    onLaunchMatch = {
                        try {
                            networkManager.launchMatchFromHost()
                            engine.initGame(
                                gameMode = GameMode.LAN_MULTIPLAYER,
                                arena = selectedArena,
                                playerName = playerName,
                                hero = selectedHero
                            )
                            currentScreen = AppScreen.IN_GAME
                            isPaused = false
                        } catch (t: Throwable) {
                            android.util.Log.e("MainActivity", "Error launching LAN match", t)
                        }
                    },
                    onBackClick = {
                        networkManager.stopAll()
                        currentScreen = AppScreen.MAIN_MENU
                    }
                )
            }

            AppScreen.CAMPAIGN_SELECT -> {
                BackHandler { currentScreen = AppScreen.MAIN_MENU }
                CampaignSelectScreen(
                    onBack = { currentScreen = AppScreen.MAIN_MENU },
                    onStartChapter = { chapter ->
                        try {
                            resetInputs()
                            engine.initGame(
                                gameMode = GameMode.CAMPAIGN_MISSION,
                                arena = chapter.arena,
                                playerName = playerName,
                                hero = chapter.playerHero,
                                chapter = chapter
                            )
                            currentScreen = AppScreen.IN_GAME
                            isPaused = false
                        } catch (t: Throwable) {
                            android.util.Log.e("MainActivity", "Error launching campaign chapter", t)
                        }
                    }
                )
            }

            AppScreen.HEROIC_CHALLENGES -> {
                BackHandler { currentScreen = AppScreen.MAIN_MENU }
                HeroicChallengesScreen(
                    onBack = { currentScreen = AppScreen.MAIN_MENU },
                    onStartChallenge = { challenge ->
                        try {
                            resetInputs()
                            engine.initGame(
                                gameMode = GameMode.HEROIC_CHALLENGE,
                                arena = challenge.arena,
                                playerName = playerName,
                                hero = challenge.playerHero,
                                challenge = challenge
                            )
                            currentScreen = AppScreen.IN_GAME
                            isPaused = false
                        } catch (t: Throwable) {
                            android.util.Log.e("MainActivity", "Error launching challenge trial", t)
                        }
                    }
                )
            }

            AppScreen.WARRIOR_CUSTOMIZATION -> {
                BackHandler { currentScreen = AppScreen.MAIN_MENU }
                WarriorCustomizationScreen(
                    currentHero = selectedHero,
                    onBackClick = { currentScreen = AppScreen.MAIN_MENU }
                )
            }

            AppScreen.IN_GAME -> {
                BackHandler {
                    isPaused = true
                }

                // Render 2D World & Characters
                GameCanvas(
                    engine = engine,
                    tick = tick,
                    modifier = Modifier.fillMaxSize()
                )

                // Render On-Screen Joysticks & HUD
                GameHudAndControls(
                    engine = engine,
                    onMoveChange = { x, y ->
                        moveX = x
                        moveY = y
                    },
                    onAimChange = { x, y ->
                        aimX = x
                        aimY = y
                    },
                    onFireChange = { isFireHeld = it },
                    onJetpackChange = { isJetpackHeld = it },
                    isFireHeld = isFireHeld,
                    isJetpackHeld = isJetpackHeld,
                    onPauseClick = { isPaused = true }
                )

                // Match Finished or Paused Dialog
                if (engine.matchEnded) {
                    GameOverDialog(
                        engine = engine,
                        onRematchClick = {
                            isPaused = false
                            engine.initGame(
                                gameMode = engine.mode,
                                arena = engine.currentArena,
                                playerName = playerName,
                                hero = selectedHero,
                                chapter = engine.currentCampaignChapter,
                                challenge = engine.currentChallenge
                            )
                        },
                        onMenuClick = {
                            isPaused = false
                            networkManager.stopAll()
                            currentScreen = AppScreen.MAIN_MENU
                        }
                    )
                } else if (isPaused) {
                    PauseDialog(
                        engine = engine,
                        onResumeClick = { isPaused = false },
                        onRestartClick = {
                            isPaused = false
                            engine.initGame(
                                gameMode = engine.mode,
                                arena = engine.currentArena,
                                playerName = playerName,
                                hero = selectedHero,
                                chapter = engine.currentCampaignChapter,
                                challenge = engine.currentChallenge
                            )
                        },
                        onMenuClick = {
                            isPaused = false
                            networkManager.stopAll()
                            currentScreen = AppScreen.MAIN_MENU
                        }
                    )
                }
            }
        }
    }
}

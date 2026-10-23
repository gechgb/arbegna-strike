package com.example

import android.content.Context
import androidx.compose.ui.geometry.Rect
import androidx.test.core.app.ApplicationProvider
import com.example.audio.*
import com.example.engine.*
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Arbegna Strike", appName)
    }

    @Test
    fun `weapons start from sparrow bow and progress to modern guns`() {
        val weapons = WeaponType.values()
        assertEquals(WeaponType.SPARROW_BOW, weapons.first())
        assertEquals(1, WeaponType.SPARROW_BOW.tier)
        assertTrue(weapons.any { it == WeaponType.WAR_SPEAR })
        assertTrue(weapons.any { it == WeaponType.AK47_KALASHNIKOV })
        assertTrue(weapons.any { it == WeaponType.RPG7_ROCKET })
        assertTrue(weapons.any { it == WeaponType.PLASMA_RIFLE })
    }

    @Test
    fun `ethiopian warrior heroes defined properly`() {
        val heroes = WarriorHero.values()
        assertTrue(heroes.any { it == WarriorHero.ARBEGNA })
        assertTrue(heroes.any { it == WarriorHero.MEHAL_SEFARI })
        assertTrue(heroes.any { it == WarriorHero.GADAA_WARRIOR })
        assertTrue(heroes.any { it == WarriorHero.AFAR_SCOUT })
    }

    @Test
    fun `gravity accelerates character downward up to terminal velocity`() {
        val player = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 200f,
            vy = 0f
        )

        // Apply 0.1s of gravity
        PhysicsEngine.applyGravity(player, isThrusting = false, dt = 0.1f)
        assertEquals(85f, player.vy, 0.1f)

        // Apply multiple seconds of gravity until terminal velocity
        repeat(20) {
            PhysicsEngine.applyGravity(player, isThrusting = false, dt = 0.1f)
        }
        assertEquals(PhysicsEngine.TERMINAL_FALL_VELOCITY, player.vy, 0.01f)
    }

    @Test
    fun `character lands on platform floor and resets downward velocity`() {
        val platform = ArenaPlatform(
            id = 1,
            bounds = Rect(left = 400f, top = 500f, right = 600f, bottom = 550f),
            isOneWay = false,
            type = PlatformType.ANCIENT_ROCK
        )

        val player = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 480f, // feet are at 480 + 28 = 508 (penetrating top surface 500)
            vy = 150f
        )

        val result = PhysicsEngine.resolveCollisions(
            player = player,
            platforms = listOf(platform),
            worldWidth = 2000f,
            worldHeight = 1000f,
            prevY = 465f, // was above platform (foot was at 465 + 28 = 493 < 500)
            dt = 0.016f
        )

        assertTrue("Character should be grounded", result.isGrounded)
        assertEquals("Downwards velocity should be zeroed upon landing", 0f, player.vy, 0.01f)
        val expectedY = platform.bounds.top - PhysicsEngine.CHARACTER_HEIGHT / 2f
        assertEquals("Character feet should be placed on platform surface", expectedY, player.y, 0.01f)
    }

    @Test
    fun `one-way platform allows jumping through from below but catches falling character`() {
        val oneWayPlatform = ArenaPlatform(
            id = 2,
            bounds = Rect(left = 400f, top = 500f, right = 600f, bottom = 520f),
            isOneWay = true,
            type = PlatformType.WOODEN_BRIDGE
        )

        val jumpingUpPlayer = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 515f,
            vy = -200f // moving upward
        )

        val jumpResult = PhysicsEngine.resolveCollisions(
            player = jumpingUpPlayer,
            platforms = listOf(oneWayPlatform),
            worldWidth = 2000f,
            worldHeight = 1000f,
            prevY = 530f,
            dt = 0.016f
        )

        // Character should NOT hit ceiling on one-way platform
        assertFalse("One-way platform should not block upward movement", jumpResult.hitCeiling)
        assertEquals(-200f, jumpingUpPlayer.vy, 0.01f)
    }

    @Test
    fun `solid wall stops horizontal penetration`() {
        val solidPlatform = ArenaPlatform(
            id = 3,
            bounds = Rect(left = 500f, top = 400f, right = 600f, bottom = 600f),
            isOneWay = false,
            type = PlatformType.CASTLE_BRICK
        )

        val playerMovingRight = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 490f, // right edge is at 490 + 20 = 510 (penetrating left wall at 500)
            y = 480f,
            vx = 200f,
            vy = 0f
        )

        val result = PhysicsEngine.resolveCollisions(
            player = playerMovingRight,
            platforms = listOf(solidPlatform),
            worldWidth = 2000f,
            worldHeight = 1000f,
            prevY = 480f,
            dt = 0.016f
        )

        assertTrue("Wall hit should be reported", result.hitWall)
        assertEquals("Horizontal velocity should be zeroed upon wall collision", 0f, playerMovingRight.vx, 0.01f)
        val expectedX = solidPlatform.bounds.left - PhysicsEngine.CHARACTER_HALF_WIDTH
        assertEquals("Player should be snapped to the wall edge", expectedX, playerMovingRight.x, 0.01f)
    }

    @Test
    fun `vertical thrust accelerates player upward and consumes fuel over time`() {
        val player = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 500f,
            vy = 0f,
            jetpackFuel = 100f
        )

        var thrustReported = false
        // Push full upward thrust (moveY = -1.0f) for 0.5s
        PhysicsEngine.applyJetpackAndGravity(player, moveY = -1.0f, dt = 0.5f) {
            thrustReported = true
        }

        assertTrue("Thrust callback should be invoked", thrustReported)
        assertTrue("Player should be in jetpacking state", player.isJetpacking)
        assertTrue("Vertical velocity should be accelerating upwards (negative vy)", player.vy < 0f)
        // Fuel should have burned: 100 - (35 * 1.0 * 0.5) = 82.5f
        assertEquals(82.5f, player.jetpackFuel, 0.1f)
    }

    @Test
    fun `jetpack thrust shuts off when fuel reaches zero`() {
        val player = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 500f,
            vy = -50f,
            jetpackFuel = 0f // Empty fuel tank
        )

        // Attempting to fly with 0 fuel
        PhysicsEngine.applyJetpackAndGravity(player, moveY = -1.0f, dt = 0.1f)

        assertFalse("Jetpack should NOT engage with empty fuel", player.isJetpacking)
        // Normal downward gravity should take over (vy increases downward)
        assertTrue("Gravity should increase vertical velocity downwards", player.vy > -50f)
    }

    @Test
    fun `super jetpack boost grants enhanced thrust without burning fuel`() {
        val player = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 500f,
            vy = 0f,
            jetpackFuel = 50f,
            superJetpackTimerMs = 5000L
        )

        PhysicsEngine.applyJetpackAndGravity(player, moveY = -1.0f, dt = 0.5f)

        assertTrue("Player should be jetpacking", player.isJetpacking)
        // Fuel should NOT have burned during turbo boost
        assertEquals(50f, player.jetpackFuel, 0.01f)
        // Super timer should have decremented
        assertEquals(4500L, player.superJetpackTimerMs)
        // Super thrust should reach a strong upward velocity
        assertTrue(player.vy <= -500f)
    }

    @Test
    fun `jetpack fuel regenerates when grounded after cooldown delay`() {
        val player = PlayerState(
            id = "test_player",
            name = "Test",
            hero = WarriorHero.ARBEGNA,
            x = 500f,
            y = 500f,
            jetpackFuel = 20f,
            jetpackRechargeDelayMs = 0L // delay expired
        )

        // Step while on ground with no vertical input
        PhysicsEngine.step(
            player = player,
            moveX = 0f,
            moveY = 0f,
            dt = 1.0f,
            platforms = emptyList(),
            worldWidth = 2000f,
            worldHeight = 580f // player at 500 with halfHeight 28 penetrates bottom limit 500 -> grounded
        )

        assertTrue("Player should be grounded", player.isGrounded)
        assertTrue("Fuel should have regenerated above initial 20", player.jetpackFuel > 20f)
    }

    @Test
    fun `quick match starts and runs stably across extreme screen aspect ratios`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = com.example.audio.GameAudio(context)
        val engine = com.example.engine.GameEngine(audio = audio)

        // Initialize Quick Match with 4 bots
        engine.initGame(
            gameMode = com.example.model.GameMode.BOT_DEATHMATCH,
            arena = com.example.model.MapArena.SIMIEN_PEAKS,
            playerName = "Abebe",
            hero = WarriorHero.ARBEGNA,
            botCount = 4
        )

        assertEquals(5, engine.players.size)
        assertEquals("Abebe", engine.localPlayer.name)

        // Simulate wide screen (2560x1080) - previously caused coerceIn IllegalArgumentException
        engine.viewWidth = 2560f
        engine.viewHeight = 1080f

        // Run 60 game update ticks with player firing
        for (i in 0 until 60) {
            engine.update(
                moveX = 0.5f,
                moveY = -0.5f,
                aimX = 1.0f,
                aimY = 0.0f,
                isFireHeld = true
            )
        }

        // Simulate narrow screen (720x1600)
        engine.viewWidth = 720f
        engine.viewHeight = 1600f

        for (i in 0 until 60) {
            engine.update(
                moveX = -0.5f,
                moveY = 0f,
                aimX = -1.0f,
                aimY = 0.2f,
                isFireHeld = (i % 2 == 0)
            )
        }

        assertTrue("Engine should remain active without crashing", engine.players.isNotEmpty())
        audio.release()
    }

    @Test
    fun `survival mode starts and processes waves without crashing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = com.example.audio.GameAudio(context)
        val engine = com.example.engine.GameEngine(audio = audio)

        // Initialize Survival Waves
        engine.initGame(
            gameMode = com.example.model.GameMode.SURVIVAL_WAVES,
            arena = com.example.model.MapArena.FASIL_FORTRESS,
            playerName = "Alula",
            hero = WarriorHero.MEHAL_SEFARI
        )

        assertEquals(1, engine.currentWave)
        assertTrue("Survival invaders should be spawned", engine.players.size > 1)

        // Run 60 update ticks
        for (i in 0 until 60) {
            engine.update(
                moveX = 0f,
                moveY = -1f,
                aimX = 0.8f,
                aimY = -0.5f,
                isFireHeld = true
            )
        }

        audio.release()
    }

    @Test
    fun `dedicated jetpack button engages vertical flight and burns fuel`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = com.example.audio.GameAudio(context)
        val engine = com.example.engine.GameEngine(audio = audio)

        engine.initGame(
            gameMode = com.example.model.GameMode.WEAPON_RANGE,
            arena = com.example.model.MapArena.SIMIEN_PEAKS,
            playerName = "FlightTest",
            hero = WarriorHero.ARBEGNA
        )

        val initialFuel = engine.localPlayer.jetpackFuel
        // Hold dedicated jetpack button without moving joystick
        engine.update(moveX = 0f, moveY = 0f, isJetpackHeld = true)

        assertTrue("Player should engage jetpack when dedicated button held", engine.localPlayer.isJetpacking)
        assertTrue("Vertical speed should accelerate upward", engine.localPlayer.vy < 0f)
        assertTrue("Fuel should be consumed during jetpack flight", engine.localPlayer.jetpackFuel < initialFuel)

        audio.release()
    }

    @Test
    fun `dual firing triggers projectiles from both aim stick deflection and dedicated fire button`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = com.example.audio.GameAudio(context)
        val engine = com.example.engine.GameEngine(audio = audio)

        engine.initGame(
            gameMode = com.example.model.GameMode.WEAPON_RANGE,
            arena = com.example.model.MapArena.SIMIEN_PEAKS,
            playerName = "Shooter",
            hero = WarriorHero.COMMANDO
        )

        // 1. Dedicated fire button trigger
        engine.update(moveX = 0f, moveY = 0f, isFireHeld = true)
        assertTrue("Projectiles should be spawned when dedicated fire button is held", engine.projectiles.isNotEmpty())

        engine.projectiles.clear()
        engine.localPlayer.nextFireAllowedMs = 0L // reset cooldown

        // 2. Aim stick auto-fire trigger (magnitude > 0.35)
        engine.update(moveX = 0f, moveY = 0f, aimX = 0.9f, aimY = 0.1f, isFireHeld = false)
        assertTrue("Projectiles should be spawned when aim stick is deflected beyond deadzone", engine.projectiles.isNotEmpty())

        audio.release()
    }

    @Test
    fun `soft aim assist magnetically pulls aim angle toward enemy target`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = GameAudio(context)
        val engine = GameEngine(audio = audio)

        engine.initGame(
            gameMode = GameMode.WEAPON_RANGE,
            arena = MapArena.SIMIEN_PEAKS,
            playerName = "Marksman",
            hero = WarriorHero.ARBEGNA
        )

        val rad25 = 25.0 * (Math.PI / 180.0)
        engine.update(
            moveX = 0f,
            moveY = 0f,
            aimX = kotlin.math.cos(rad25).toFloat(),
            aimY = kotlin.math.sin(rad25).toFloat()
        )

        assertTrue("Aim angle should have been pulled toward target dummy", engine.localPlayer.aimAngleDeg < 25f)
        assertTrue("Aim angle should remain positive", engine.localPlayer.aimAngleDeg >= 0f)

        audio.release()
    }

    @Test
    fun `test bot deathmatch simulation for 120 ticks with all arenas and all bot counts`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = GameAudio(context)
        val engine = GameEngine(audio = audio)

        for (arena in MapArena.values()) {
            for (botCount in 1..5) {
                engine.initGame(
                    gameMode = GameMode.BOT_DEATHMATCH,
                    arena = arena,
                    playerName = "Patriot",
                    hero = WarriorHero.ARBEGNA,
                    botCount = botCount
                )
                repeat(120) {
                    engine.update(moveX = 0.5f, moveY = -0.5f, aimX = 1f, aimY = 0f, isFireHeld = true)
                }
            }
        }
        audio.release()
    }

    @Test
    fun `test historical campaign chapters repository progression and star unlocking`() {
        val chapters = CampaignRepository.chapters
        assertEquals(4, chapters.size)
        assertEquals("Battle of Gundet", chapters[0].title)
        assertTrue(chapters[0].dateText.contains("1875"))
        assertTrue(chapters[1].dateText.contains("1887"))
        assertTrue(chapters[2].dateText.contains("1896"))
        assertTrue(chapters[3].dateText.contains("1941"))

        assertTrue("First chapter should be unlocked by default", chapters[0].isUnlocked)

        // Complete chapter 1 with 3 stars
        CampaignRepository.markChapterCompleted(1, 3)
        assertTrue("Chapter 2 should unlock after completing chapter 1", chapters[1].isUnlocked)
        assertEquals(3, chapters[0].starsEarned)
        assertTrue(CampaignRepository.getTotalStarsEarned() >= 3)
    }

    @Test
    fun `test campaign mission gameplay simulation for 60 ticks`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = GameAudio(context)
        val engine = GameEngine(audio = audio)

        val adwaChapter = CampaignRepository.chapters[2]
        engine.initGame(
            gameMode = GameMode.CAMPAIGN_MISSION,
            arena = adwaChapter.arena,
            playerName = "Menelik Warrior",
            hero = adwaChapter.playerHero,
            chapter = adwaChapter
        )

        assertEquals(adwaChapter, engine.currentCampaignChapter)
        assertEquals(GameMode.CAMPAIGN_MISSION, engine.mode)
        assertTrue("Campaign enemies should be spawned", engine.players.count { it.isBot } > 0)

        repeat(60) {
            engine.update(moveX = 0.8f, moveY = -0.5f, aimX = 1f, aimY = 0f, isFireHeld = true)
        }

        assertFalse("Match should not end within 1 second", engine.matchEnded)
        audio.release()
    }

    @Test
    fun `test heroic challenge repository definitions and progression`() {
        val challenges = ChallengeRepository.challenges
        assertEquals(4, challenges.size)
        val shotelChallenge = challenges[0]
        assertEquals("Shotel Duelist", shotelChallenge.title)
        assertTrue(shotelChallenge.isUnlocked)
        assertEquals(WeaponType.SHOTEL_BLADE, shotelChallenge.lockedWeapon)

        // Complete challenge with fast time remaining -> 3 stars
        val stars = ChallengeRepository.markCompleted(challengeId = 1, kills = 8, timeRemainingSec = 75f)
        assertEquals(3, stars)
        assertEquals(3, shotelChallenge.starsEarned)
        assertTrue(shotelChallenge.highScore > 0)
        // Challenge 2 should be unlocked
        assertTrue(challenges[1].isUnlocked)
    }

    @Test
    fun `test heroic challenge gameplay simulation with infinite jetpack modifier`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = GameAudio(context)
        val engine = GameEngine(audio = audio)

        val rocketeerChallenge = ChallengeRepository.challenges[2] // Fasil Rocketeer
        engine.initGame(
            gameMode = GameMode.HEROIC_CHALLENGE,
            arena = rocketeerChallenge.arena,
            playerName = "Fasil Falcon",
            hero = rocketeerChallenge.playerHero,
            challenge = rocketeerChallenge
        )

        assertEquals(rocketeerChallenge, engine.currentChallenge)
        assertEquals(GameMode.HEROIC_CHALLENGE, engine.mode)
        assertEquals(WeaponType.RPG7_ROCKET, engine.localPlayer.currentWeapon)

        // Simulate flight - jetpack fuel should remain full with infinite jetpack modifier
        repeat(60) {
            engine.update(moveX = 0f, moveY = -1f, aimX = 1f, aimY = 0f, isFireHeld = false, isJetpackHeld = true)
        }

        assertEquals(engine.localPlayer.maxJetpackFuel, engine.localPlayer.jetpackFuel, 0.1f)
        assertFalse(engine.matchEnded)
        audio.release()
    }

    @Test
    fun `test cultural attire cosmetic customization repository and presets`() {
        val repo = WarriorCustomizationRepository
        // Test initial or default attire
        repo.equipCape(CapeSkin.ROYAL_JANO)
        repo.equipKumeta(KumetaSkin.ROYAL_NAVY_KUMETA)
        repo.equipHeadgear(HeadgearSkin.LION_MANE_GOFERE)
        repo.equipSash(SashSkin.ROYAL_JANO_SASH)

        assertEquals(CapeSkin.ROYAL_JANO, repo.currentCustomization.cape)
        assertEquals(KumetaSkin.ROYAL_NAVY_KUMETA, repo.currentCustomization.kumeta)
        assertEquals(HeadgearSkin.LION_MANE_GOFERE, repo.currentCustomization.headgear)
        assertEquals(SashSkin.ROYAL_JANO_SASH, repo.currentCustomization.sash)

        // Test Preset: Arbegna Mountain Patriot
        val patriotPreset = repo.presets.first { it.id == "arbegna_patriot" }
        repo.equipPreset(patriotPreset)

        assertEquals(CapeSkin.PATRIOT_KUTA, repo.currentCustomization.cape)
        assertEquals(KumetaSkin.PATRIOT_KHAKI_KUMETA, repo.currentCustomization.kumeta)
        assertEquals(HeadgearSkin.LION_MANE_GOFERE, repo.currentCustomization.headgear)
        assertEquals(SashSkin.PATRIOT_CARTRIDGE_BELT, repo.currentCustomization.sash)
    }

    @Test
    fun `test custom attire equips onto local player in game engine`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val audio = GameAudio(context)
        val engine = GameEngine(audio = audio)

        // Equip Velvet Kabba and Midnight Onyx Kumeta
        WarriorCustomizationRepository.equipCape(CapeSkin.BLACK_VELVET_CABBA)
        WarriorCustomizationRepository.equipKumeta(KumetaSkin.MIDNIGHT_ONYX_KUMETA)

        engine.initGame(
            gameMode = GameMode.BOT_DEATHMATCH,
            arena = MapArena.FASIL_FORTRESS,
            playerName = "Axumite Guard",
            hero = WarriorHero.MEHAL_SEFARI
        )

        assertEquals(CapeSkin.BLACK_VELVET_CABBA, engine.localPlayer.customization.cape)
        assertEquals(KumetaSkin.MIDNIGHT_ONYX_KUMETA, engine.localPlayer.customization.kumeta)

        audio.release()
    }
}


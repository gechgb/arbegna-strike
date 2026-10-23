package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Historical Ethiopian battle chapters for the Campaign Story Mode (የታሪክ ዘመቻ).
 */
data class CampaignChapter(
    val id: Int,
    val title: String,
    val amharicTitle: String,
    val dateText: String,
    val leaderName: String,
    val description: String,
    val objectiveDescription: String,
    val arena: MapArena,
    val playerHero: WarriorHero,
    val targetKills: Int,
    val timeLimitSec: Float,
    val bannerColor: Color,
    val enemyHero: WarriorHero = WarriorHero.COMMANDO,
    var isUnlocked: Boolean = false,
    var starsEarned: Int = 0,
    var highScore: Int = 0
)

object CampaignRepository {
    val chapters = mutableListOf(
        CampaignChapter(
            id = 1,
            title = "Battle of Gundet",
            amharicTitle = "የጉንደት ድል (1875)",
            dateText = "November 16, 1875",
            leaderName = "Emperor Yohannes IV & Ras Alula",
            description = "Foreign expeditionary forces invaded through the northern highlands. Mobilize the highland warriors with traditional curved shotels, spears, and sparrow bows to repel the invaders.",
            objectiveDescription = "Defeat 8 invaders in the Simien peaks",
            arena = MapArena.SIMIEN_PEAKS,
            playerHero = WarriorHero.ARBEGNA,
            targetKills = 8,
            timeLimitSec = 180f,
            bannerColor = Color(0xFFD97706),
            isUnlocked = true,
            starsEarned = 0
        ),
        CampaignChapter(
            id = 2,
            title = "The Ambush at Dogali",
            amharicTitle = "የዶጋሊ ጀግኖች (1887)",
            dateText = "January 26, 1887",
            leaderName = "Ras Alula Aba Nega (አባ ነጋ)",
            description = "Italian colonial infantry advanced through Dogali. Under Ras Alula's tactical genius, guerrilla patriots struck from hidden ridges using early captured muskets and spears.",
            objectiveDescription = "Eliminate 10 colonial soldiers before time runs out",
            arena = MapArena.LALIBELA_RUINS,
            playerHero = WarriorHero.MEHAL_SEFARI,
            targetKills = 10,
            timeLimitSec = 180f,
            bannerColor = Color(0xFFB45309),
            isUnlocked = false,
            starsEarned = 0
        ),
        CampaignChapter(
            id = 3,
            title = "The Triumph of Adwa",
            amharicTitle = "የዐድዋ ታላቅ ድል (1896)",
            dateText = "March 1, 1896",
            leaderName = "Emperor Menelik II & Empress Taytu",
            description = "The defining battle of African sovereignty. Over 100,000 unified Ethiopians wielded legendary Martini-Henry (Wechefo) rifles and mountain cannons to decisively crush the invading army.",
            objectiveDescription = "Defeat 12 enemy infantry in the fortress valley",
            arena = MapArena.SIMIEN_PEAKS,
            playerHero = WarriorHero.GADAA_WARRIOR,
            targetKills = 12,
            timeLimitSec = 210f,
            bannerColor = Color(0xFF10B981),
            isUnlocked = false,
            starsEarned = 0
        ),
        CampaignChapter(
            id = 4,
            title = "Arbegna Fortress Liberation",
            amharicTitle = "የአርበኞች ምሽግ ነፃነት (1941)",
            dateText = "May 5, 1941",
            leaderName = "Jagama Kello & Guerrilla Patriots",
            description = "During five years of occupation, Arbegna patriots waged relentless asymmetrical mountain warfare. Storm the colonial garrison stronghold and liberate the ancient imperial fortress.",
            objectiveDescription = "Infiltrate fortress and eliminate 15 elite commanders",
            arena = MapArena.FASIL_FORTRESS,
            playerHero = WarriorHero.COMMANDO,
            targetKills = 15,
            timeLimitSec = 240f,
            bannerColor = Color(0xFF2563EB),
            isUnlocked = false,
            starsEarned = 0
        )
    )

    fun getChapter(id: Int): CampaignChapter? = chapters.find { it.id == id }

    fun getTotalStarsEarned(): Int = chapters.sumOf { it.starsEarned }

    fun markChapterCompleted(chapterId: Int, stars: Int): Int {
        val chapter = getChapter(chapterId) ?: return 0
        if (stars > chapter.starsEarned) {
            chapter.starsEarned = stars
        }
        val nextChapter = chapters.find { it.id == chapterId + 1 }
        nextChapter?.isUnlocked = true
        return stars
    }

    fun markChapterCompleted(chapterId: Int, kills: Int, timeRemainingSec: Float): Int {
        val chapter = getChapter(chapterId) ?: return 0
        val stars = when {
            timeRemainingSec >= 60f -> 3
            timeRemainingSec >= 20f -> 2
            else -> 1
        }
        if (stars > chapter.starsEarned) {
            chapter.starsEarned = stars
        }
        val score = (kills * 100) + (timeRemainingSec * 10).toInt()
        if (score > chapter.highScore) {
            chapter.highScore = score
        }
        // Unlock next chapter
        val nextChapter = chapters.find { it.id == chapterId + 1 }
        nextChapter?.isUnlocked = true
        return stars
    }
}

/**
 * Combat challenges with specialized weapon modifiers, infinite jetpack aerial dogfights,
 * and high-stakes survival trials.
 */
data class HeroicChallenge(
    val id: Int,
    val title: String,
    val amharicTitle: String,
    val badge: String,
    val description: String,
    val modifierName: String,
    val modifierDescription: String,
    val targetKills: Int,
    val timeLimitSec: Float,
    val arena: MapArena,
    val playerHero: WarriorHero,
    val lockedWeapon: WeaponType,
    val hasInfiniteJetpack: Boolean = false,
    val bonusShield: Int = 0,
    var isUnlocked: Boolean = false,
    var starsEarned: Int = 0,
    var highScore: Int = 0
)

object ChallengeRepository {
    val challenges = mutableListOf(
        HeroicChallenge(
            id = 1,
            title = "Shotel Duelist",
            amharicTitle = "የሾተል ጀግና (Melee Duel)",
            badge = "Sword Mastery",
            description = "Wield the traditional curved double-edged Shotel sword. Dash into hostile infantry and slice through defensive shields.",
            modifierName = "BLADE ONLY",
            modifierDescription = "Only Shotel Sword dash slashes allowed. +50% dash velocity.",
            targetKills = 8,
            timeLimitSec = 120f,
            arena = MapArena.LALIBELA_RUINS,
            playerHero = WarriorHero.ARBEGNA,
            lockedWeapon = WeaponType.SHOTEL_BLADE,
            hasInfiniteJetpack = false,
            isUnlocked = true,
            starsEarned = 0
        ),
        HeroicChallenge(
            id = 2,
            title = "Simien Ridge Sniper",
            amharicTitle = "የስሜን ንሥር (Eagle Sniper)",
            badge = "Precision Marksmanship",
            description = "High elevation tactical sniping. Pick off enemies with Dragunov sniper rifles across the Simien precipices.",
            modifierName = "SNIPER DUEL",
            modifierDescription = "Dragunov precision laser rifle only. 1-shot high lethality.",
            targetKills = 10,
            timeLimitSec = 150f,
            arena = MapArena.SIMIEN_PEAKS,
            playerHero = WarriorHero.AFAR_SCOUT,
            lockedWeapon = WeaponType.SNIPER_DRAGUNOV,
            hasInfiniteJetpack = false,
            isUnlocked = true,
            starsEarned = 0
        ),
        HeroicChallenge(
            id = 3,
            title = "Fasil Rocketeer",
            amharicTitle = "የፋሲል መድፈኛ (Aerial Artillery)",
            badge = "Infinite Jetpack",
            description = "High-flying aerial dogfight above Gondar's castle battlements. Infinite turbo jetpack fuel and explosive RPG-7 rockets.",
            modifierName = "TURBO FLIGHT & RPGS",
            modifierDescription = "Infinite Jetpack thrust fuel + heavy RPG-7 rocket launchers.",
            targetKills = 12,
            timeLimitSec = 150f,
            arena = MapArena.FASIL_FORTRESS,
            playerHero = WarriorHero.COMMANDO,
            lockedWeapon = WeaponType.RPG7_ROCKET,
            hasInfiniteJetpack = true,
            isUnlocked = false,
            starsEarned = 0
        ),
        HeroicChallenge(
            id = 4,
            title = "Imperial Bronze Shield",
            amharicTitle = "የነሐስ ጋሻ ጥበቃ (Sanctuary Defense)",
            badge = "Heavy Fortress",
            description = "Hold the sacred ground in Axum against waves of invaders. Augmented bronze shield with Martini-Henry rifles.",
            modifierName = "SUPER SHIELD DEFENSE",
            modifierDescription = "Start with +100 Max Armor. Rapid enemy respawns.",
            targetKills = 14,
            timeLimitSec = 180f,
            arena = MapArena.LALIBELA_RUINS,
            playerHero = WarriorHero.MEHAL_SEFARI,
            lockedWeapon = WeaponType.MARTINI_HENRY,
            bonusShield = 100,
            isUnlocked = false,
            starsEarned = 0
        )
    )

    fun getChallenge(id: Int): HeroicChallenge? = challenges.find { it.id == id }

    fun getTotalStars(): Int = challenges.sumOf { it.starsEarned }

    fun markCompleted(challengeId: Int, kills: Int, timeRemainingSec: Float): Int {
        val ch = getChallenge(challengeId) ?: return 0
        val stars = when {
            timeRemainingSec >= 50f -> 3
            timeRemainingSec >= 15f -> 2
            else -> 1
        }
        if (stars > ch.starsEarned) {
            ch.starsEarned = stars
        }
        val score = (kills * 120) + (timeRemainingSec * 15).toInt()
        if (score > ch.highScore) {
            ch.highScore = score
        }
        val next = challenges.find { it.id == challengeId + 1 }
        next?.isUnlocked = true
        return stars
    }
}

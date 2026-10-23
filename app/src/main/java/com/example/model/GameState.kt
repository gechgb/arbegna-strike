package com.example.model

import androidx.compose.ui.graphics.Color

enum class PickupType(val title: String, val amharicName: String, val color: Color) {
    HEALTH_TENADAM("Tenadam Herbal Meds", "ጤናዳም መድኃኒት", Color(0xFF10B981)),
    SHIELD_GASHAA("Gashaa Shield", "የነሐስ ጋሻ", Color(0xFF38BDF8)),
    AMMO_CRATE("Ammunition Crate", "የጥይት ሣጥን", Color(0xFFF59E0B)),
    SUPER_JETPACK("Jetpack Turbo Boost", "የሮኬት ነዳጅ", Color(0xFFFF6D00)),
    WEAPON_DROP("Weapon Crate", "የጦር መሣሪያ", Color(0xFFA855F7))
}

data class PickupEntity(
    val id: Int,
    val type: PickupType,
    val weaponType: WeaponType? = null,
    var x: Float,
    var y: Float,
    var isAvailable: Boolean = true,
    var respawnTimerMs: Long = 0L
)

data class ProjectileEntity(
    val id: Long,
    val shooterId: String,
    val weaponType: WeaponType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val gravity: Float,
    val damage: Int,
    val radius: Float,
    var lifeTimeMs: Long,
    val maxLifeTimeMs: Long,
    val isExplosive: Boolean,
    val blastRadius: Float,
    val color: Color
)

data class ParticleEntity(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var radius: Float,
    var lifeTimeMs: Long,
    val maxLifeTimeMs: Long
)

data class DamagePopup(
    val id: Long,
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var lifeTimeMs: Long
)

data class KillEvent(
    val killerName: String,
    val victimName: String,
    val weaponTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PlayerState(
    val id: String,
    var name: String,
    var hero: WarriorHero,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var aimAngleDeg: Float = 0f,
    var isFacingRight: Boolean = true,
    var isJetpacking: Boolean = false,
    var jetpackFuel: Float = 100f,
    val maxJetpackFuel: Float = 100f,
    var health: Int = 100,
    var maxHealth: Int = 100,
    var armor: Int = 50,
    var maxArmor: Int = 50,
    var currentWeapon: WeaponType = WeaponType.SPARROW_BOW,
    var secondaryWeapon: WeaponType = WeaponType.WAR_SPEAR,
    var ammoInMag: Int = 12,
    var reserveAmmo: Int = 60,
    var isReloading: Boolean = false,
    var reloadTimerMs: Long = 0L,
    var nextFireAllowedMs: Long = 0L,
    var meleeCooldownMs: Long = 0L,
    var isDead: Boolean = false,
    var respawnTimerMs: Long = 0L,
    var kills: Int = 0,
    var deaths: Int = 0,
    var isBot: Boolean = false,
    var lastDamagedById: String = "",
    var isInvulnerableMs: Long = 0L,
    var isGrounded: Boolean = false,
    var superJetpackTimerMs: Long = 0L,
    var jetpackRechargeDelayMs: Long = 0L,
    var customization: WarriorCustomization = WarriorCustomization()
)

enum class GameMode(val title: String, val amharicName: String, val description: String) {
    BOT_DEATHMATCH(
        "Bot Deathmatch",
        "የቦት ውጊያ",
        "Battle against intelligent Ethiopian AI warriors with jetpacks, rifles, and bows in classic free-for-all."
    ),
    SURVIVAL_WAVES(
        "Fortress Survival",
        "የምሽግ መከላከያ",
        "Defend the mountain outpost against waves of incoming hostile invaders. Upgrades after each wave!"
    ),
    LAN_MULTIPLAYER(
        "LAN Multiplayer",
        "የአካባቢ መረብ (LAN)",
        "Mini Militia style local Wi-Fi / Hotspot multiplayer! Host or join matches with nearby friends."
    ),
    WEAPON_RANGE(
        "Weapons Armory",
        "የጦር ልምምድ",
        "Test fire and master all 10 historical and modern Ethiopian weapons with infinite targets and ammo."
    ),
    CAMPAIGN_MISSION(
        "Campaign Story",
        "የታሪክ ዘመቻ",
        "Relive monumental historical Ethiopian victories across epic story chapters with authentic weapons and objectives."
    ),
    HEROIC_CHALLENGE(
        "Heroic Challenges",
        "የጀግንነት ፈተናዎች",
        "Master high-stakes combat challenges with unique weapon modifiers, infinite jetpack aerial duels, and speedrun trials."
    )
}

enum class HapticType {
    LIGHT_FIRE,
    HEAVY_FIRE,
    EXPLOSION,
    DAMAGE_TAKEN
}


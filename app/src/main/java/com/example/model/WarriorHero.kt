package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Ethiopian warrior hero characters with historical and modern backgrounds,
 * unique combat perks, and visual themes.
 */
enum class WarriorHero(
    val title: String,
    val amharicName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val headgearType: String,
    val perkDescription: String,
    val speedMultiplier: Float = 1.0f,
    val maxArmorBonus: Int = 0,
    val jetpackRechargeRate: Float = 1.0f,
    val meleeDamageBonus: Int = 0,
    val initialWeapon: WeaponType = WeaponType.SPARROW_BOW
) {
    ARBEGNA(
        title = "Arbegna (Patriot)",
        amharicName = "አርበኛ",
        description = "Legendary Ethiopian anti-fascist guerrilla warrior dressed in traditional netela and lion mane headdress.",
        primaryColor = Color(0xFFD97706),
        secondaryColor = Color(0xFF10B981),
        headgearType = "LION_MANE",
        perkDescription = "+15% Sprint Speed & Bravery",
        speedMultiplier = 1.15f,
        initialWeapon = WeaponType.SPARROW_BOW
    ),
    MEHAL_SEFARI(
        title = "Mehal Sefari (Royal Guard)",
        amharicName = "መሃል ሰፋሪ",
        description = "Imperial palace vanguard equipped with ornate golden battle attire and high resilience armor.",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFF78350F),
        headgearType = "IMPERIAL_HELM",
        perkDescription = "+30 Max Armor Shielding",
        maxArmorBonus = 30,
        initialWeapon = WeaponType.WAR_SPEAR
    ),
    GADAA_WARRIOR(
        title = "Gadaa Warrior",
        amharicName = "የገዳ ጀግና",
        description = "Swift warrior bearing the sacred Kallacha ornament with extreme agility and fast jetpack recovery.",
        primaryColor = Color(0xFFDC2626),
        secondaryColor = Color(0xFF111827),
        headgearType = "KALLACHA",
        perkDescription = "+25% Jetpack Recharge Rate",
        jetpackRechargeRate = 1.25f,
        initialWeapon = WeaponType.SPARROW_BOW
    ),
    AFAR_SCOUT(
        title = "Afar Nomad Scout",
        amharicName = "የአፋር ስካውት",
        description = "Fierce Danakil desert nomad master of the curved Gile dagger with lethal close-quarter strikes.",
        primaryColor = Color(0xFFEA580C),
        secondaryColor = Color(0xFFFEF08A),
        headgearType = "DESERT_TURBAN",
        perkDescription = "+35 Melee Slash Damage",
        meleeDamageBonus = 35,
        initialWeapon = WeaponType.SHOTEL_BLADE
    ),
    SIMIEN_HUNTER(
        title = "Simien Eagle Hunter",
        amharicName = "የሰሜን አዳኝ",
        description = "Mountain sharpshooter trained in the jagged cliffs of Simien with pinpoint long-range accuracy.",
        primaryColor = Color(0xFF059669),
        secondaryColor = Color(0xFFD1D5DB),
        headgearType = "EAGLE_HOOD",
        perkDescription = "+20% Arrow & Bullet Velocity",
        speedMultiplier = 1.05f,
        initialWeapon = WeaponType.SPARROW_BOW
    ),
    COMMANDO(
        title = "Special Commando",
        amharicName = "ልዩ ኮማንዶ",
        description = "Modern Ethiopian tactical commando with night-vision tactical helmet, kevlar vest, and combat boots.",
        primaryColor = Color(0xFF2563EB),
        secondaryColor = Color(0xFF1E293B),
        headgearType = "TACTICAL_HELMET",
        perkDescription = "+20 Max Armor & Faster Reload",
        maxArmorBonus = 20,
        initialWeapon = WeaponType.AK47_KALASHNIKOV
    )
}

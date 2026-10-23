package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Cultural Ethiopian Cape & Mantle Attire (ኩታና ካባ)
 */
enum class CapeSkin(
    val id: String,
    val title: String,
    val amharicName: String,
    val description: String,
    val capeColor: Color,
    val tibebColor: Color,
    val trimColor: Color,
    val categoryBadge: String = "Imperial Attire"
) {
    ROYAL_JANO(
        id = "royal_jano",
        title = "Imperial Royal Jano",
        amharicName = "የንጉሠ ነገሥት ጃኖ ኩታ",
        description = "Iconic Ethiopian ceremonial white handspun cotton Kuta with royal crimson and gold Tibeb border.",
        capeColor = Color(0xFFF8FAFC),
        tibebColor = Color(0xFFDC2626), // Imperial Crimson
        trimColor = Color(0xFFF59E0B),  // Ethiopian Gold
        categoryBadge = "Imperial Jano"
    ),
    PATRIOT_KUTA(
        id = "patriot_kuta",
        title = "Arbegna White Kuta",
        amharicName = "የአርበኛ ነጭ ኩታ",
        description = "Pristine highland Shemma cotton draped mantle with national green, gold, and red woven border.",
        capeColor = Color(0xFFF1F5F9),
        tibebColor = Color(0xFF10B981), // Emerald Green
        trimColor = Color(0xFFDC2626),  // Crimson
        categoryBadge = "Patriot Classic"
    ),
    BLACK_VELVET_CABBA(
        id = "black_velvet_cabba",
        title = "Imperial Velvet Kabba",
        amharicName = "የጥቁር ቬልቬት ካባ",
        description = "Regal black velvet coronation cloak embroidered with intricate Axumite gold filigree patterns.",
        capeColor = Color(0xFF0F172A),  // Deep Obsidian Velvet
        tibebColor = Color(0xFFF59E0B), // Pure Gold
        trimColor = Color(0xFFFDE68A),  // Bright Gold Trim
        categoryBadge = "Imperial Court"
    ),
    SHEPHERD_GABI(
        id = "shepherd_gabi",
        title = "Highland Warm Gabi",
        amharicName = "የደጋ ጋቢ ካባ",
        description = "Double-woven thick cotton warrior wrap designed to withstand freezing mountain winds atop Ras Dashen.",
        capeColor = Color(0xFFE2E8F0),
        tibebColor = Color(0xFFB45309), // Earth Ochre
        trimColor = Color(0xFF78350F),
        categoryBadge = "Simien Highland"
    ),
    DESERT_NETELA(
        id = "desert_netela",
        title = "Danakil Nomad Netela",
        amharicName = "የአፋር በረሃ ነጠላ",
        description = "Lightweight amber cotton gauze worn by desert warriors to deflect heat and dust storms.",
        capeColor = Color(0xFFFEF3C7),  // Warm Sand
        tibebColor = Color(0xFFEA580C), // Desert Amber
        trimColor = Color(0xFFF59E0B),
        categoryBadge = "Nomad Scout"
    )
}

/**
 * Cultural Ethiopian Kumeta Riding Trousers & Boots (ቁሜታና ቦት ጫማ)
 */
enum class KumetaSkin(
    val id: String,
    val title: String,
    val amharicName: String,
    val description: String,
    val trouserColor: Color,
    val fringeColor: Color,
    val bootColor: Color,
    val buckleColor: Color = Color(0xFFFDE68A)
) {
    ROYAL_NAVY_KUMETA(
        id = "royal_navy_kumeta",
        title = "Imperial Court Kumeta",
        amharicName = "የቤተ-መንግሥት ቁሜታ",
        description = "Royal navy riding trousers with golden silk cuff embroidery and burnished brass thruster buckles.",
        trouserColor = Color(0xFF1E293B),
        fringeColor = Color(0xFFF59E0B),
        bootColor = Color(0xFF334155),
        buckleColor = Color(0xFFFDE68A)
    ),
    PATRIOT_KHAKI_KUMETA(
        id = "patriot_khaki_kumeta",
        title = "Arbegna Khaki Kumeta",
        amharicName = "የአርበኛ ካኪ ቁሜታ",
        description = "Rugged highland combat khaki trousers reinforced with leather thigh patches for mountain warfare.",
        trouserColor = Color(0xFF785938),
        fringeColor = Color(0xFFD97706),
        bootColor = Color(0xFF452E1B),
        buckleColor = Color(0xFFD4D4D8)
    ),
    CRIMSON_TIBEB_KUMETA(
        id = "crimson_tibeb_kumeta",
        title = "Crimson Tibeb Kumeta",
        amharicName = "የቀይ ጥበብ ቁሜታ",
        description = "Ceremonial deep crimson trousers with stepped diamond Tibeb geometric cuffs.",
        trouserColor = Color(0xFF7F1D1D),
        fringeColor = Color(0xFFF59E0B),
        bootColor = Color(0xFF450A0A),
        buckleColor = Color(0xFFFDE68A)
    ),
    MIDNIGHT_ONYX_KUMETA(
        id = "midnight_onyx_kumeta",
        title = "Midnight Commando Kumeta",
        amharicName = "የለሊት አርበኛ ቁሜታ",
        description = "Stealth obsidian tactical trousers with silent composite soles for night skirmishes.",
        trouserColor = Color(0xFF090D16),
        fringeColor = Color(0xFF38BDF8),
        bootColor = Color(0xFF1E293B),
        buckleColor = Color(0xFF38BDF8)
    )
}

/**
 * Cultural Headgear & Royal Crowns (የራስ ቁርና ጎፈሬ)
 */
enum class HeadgearSkin(
    val id: String,
    val title: String,
    val amharicName: String,
    val description: String,
    val headgearType: String, // Matches rendering logic in GameCanvas
    val primaryColor: Color,
    val accentColor: Color
) {
    HERO_DEFAULT(
        id = "hero_default",
        title = "Hero Archetype Crest",
        amharicName = "የጀግናው መደበኛ የራስ ቁር",
        description = "Original battle headdress designated for this warrior archetype.",
        headgearType = "HERO_DEFAULT",
        primaryColor = Color(0xFFF59E0B),
        accentColor = Color(0xFFDC2626)
    ),
    LION_MANE_GOFERE(
        id = "lion_mane_gofere",
        title = "Arbegna Lion Mane (Gofere)",
        amharicName = "የአንበሳ ጎፈሬ",
        description = "Fierce lion mane headdress bestowed upon legendary Ethiopian patriots who slayed the beast in single combat.",
        headgearType = "LION_MANE",
        primaryColor = Color(0xFFB45309),
        accentColor = Color(0xFFF59E0B)
    ),
    IMPERIAL_GOLD_HELM(
        id = "imperial_gold_helm",
        title = "Imperial Guard Crown Helm",
        amharicName = "የወርቅ ቤተ-መንግሥት ቁር",
        description = "Heavy gilded bronze helmet adorned with the radiant Axumite sun cross crest.",
        headgearType = "IMPERIAL_HELM",
        primaryColor = Color(0xFFF59E0B),
        accentColor = Color(0xFFDC2626)
    ),
    GADA_KALLACHA(
        id = "gada_kallacha",
        title = "Sacred Gadaa Kallacha",
        amharicName = "የገዳ ቅዱስ ቃላቻ",
        description = "Sacred polished metal forehead emblem worn by Gadaa leaders signifying truth, justice, and bravery.",
        headgearType = "KALLACHA",
        primaryColor = Color(0xFFFDE68A),
        accentColor = Color(0xFFD97706)
    ),
    DESERT_TAGELMUST(
        id = "desert_tagelmust",
        title = "Afar Desert Turban",
        amharicName = "የበረሃ ጥምጥም",
        description = "Wrapped desert scout turban with protective sand-shield goggles.",
        headgearType = "DESERT_TURBAN",
        primaryColor = Color(0xFFD97706),
        accentColor = Color(0xFFFEF08A)
    ),
    TRI_COLOR_HEADBAND(
        id = "tri_color_headband",
        title = "Patriot Victory Ribbon",
        amharicName = "ባለሶስት ቀለም የድል ሪባን",
        description = "Silk green, yellow, and red braided headband tied with victory knots.",
        headgearType = "TRI_HEADBAND",
        primaryColor = Color(0xFF10B981),
        accentColor = Color(0xFFDC2626)
    )
}

/**
 * Chest Sash & Cartridge Bandoliers (የደረት ድግና ማዕረግ)
 */
enum class SashSkin(
    val id: String,
    val title: String,
    val amharicName: String,
    val description: String,
    val bandColor: Color,
    val stripeColor: Color,
    val width: Float = 5f
) {
    ROYAL_JANO_SASH(
        id = "royal_jano_sash",
        title = "Imperial Jano Sash",
        amharicName = "የጃኖ የደረት ድግ",
        description = "Royal red silk sash with woven golden Tibeb geometric borders worn across the chest.",
        bandColor = Color(0xFFDC2626),
        stripeColor = Color(0xFFFDE68A),
        width = 5f
    ),
    PATRIOT_CARTRIDGE_BELT(
        id = "patriot_cartridge_belt",
        title = "Arbegna Cartridge Bandolier",
        amharicName = "የጥይት ድግ ባንዶሌር",
        description = "Heavy oiled brown leather rifle cartridge bandolier crossed over chest with brass bullets.",
        bandColor = Color(0xFF713F12),
        stripeColor = Color(0xFFF59E0B),
        width = 6f
    ),
    TRI_COLOR_TIBEB(
        id = "tri_color_tibeb",
        title = "National Flag Tibeb",
        amharicName = "ባንዲራ ጥበብ ድግ",
        description = "Ceremonial tri-color sash bearing the proud Green, Yellow, and Red of Ethiopia.",
        bandColor = Color(0xFF10B981),
        stripeColor = Color(0xFFF59E0B),
        width = 5.5f
    ),
    GOLDEN_ORDER_CHAIN(
        id = "golden_order_chain",
        title = "Imperial Order Collar",
        amharicName = "የወርቅ ኒሻን ድግ",
        description = "Gilded ceremonial metal collar awarded for extraordinary valor in national defense.",
        bandColor = Color(0xFFF59E0B),
        stripeColor = Color(0xFFFEF3C7),
        width = 4.5f
    )
}

/**
 * Full Cosmetic Attire Configuration for a Warrior Character
 */
data class WarriorCustomization(
    var cape: CapeSkin = CapeSkin.ROYAL_JANO,
    var kumeta: KumetaSkin = KumetaSkin.ROYAL_NAVY_KUMETA,
    var headgear: HeadgearSkin = HeadgearSkin.HERO_DEFAULT,
    var sash: SashSkin = SashSkin.ROYAL_JANO_SASH
)

/**
 * Pre-configured Cultural Attire Bundles
 */
data class AttirePreset(
    val id: String,
    val name: String,
    val amharicName: String,
    val description: String,
    val cape: CapeSkin,
    val kumeta: KumetaSkin,
    val headgear: HeadgearSkin,
    val sash: SashSkin
)

object WarriorCustomizationRepository {
    var currentCustomization: WarriorCustomization = WarriorCustomization()

    val presets = listOf(
        AttirePreset(
            id = "emperor_jano",
            name = "Royal Emperor Jano",
            amharicName = "ሙሉ የንጉሥ ጃኖ ልብስ",
            description = "The highest imperial regalia: ceremonial white Jano Kuta, navy Kumeta, gold crown helm, and royal crimson sash.",
            cape = CapeSkin.ROYAL_JANO,
            kumeta = KumetaSkin.ROYAL_NAVY_KUMETA,
            headgear = HeadgearSkin.IMPERIAL_GOLD_HELM,
            sash = SashSkin.ROYAL_JANO_SASH
        ),
        AttirePreset(
            id = "arbegna_patriot",
            name = "Arbegna Mountain Patriot",
            amharicName = "የአርበኛ ኩታና ጎፈሬ",
            description = "Authentic attire of 1941 patriots: pure cotton Kuta, rugged khaki Kumeta, lion-mane Gofere, and cartridge bandolier.",
            cape = CapeSkin.PATRIOT_KUTA,
            kumeta = KumetaSkin.PATRIOT_KHAKI_KUMETA,
            headgear = HeadgearSkin.LION_MANE_GOFERE,
            sash = SashSkin.PATRIOT_CARTRIDGE_BELT
        ),
        AttirePreset(
            id = "imperial_black_velvet",
            name = "Axumite Night Guard",
            amharicName = "የአክሱም ለሊት ዘበኛ",
            description = "Aristocratic velvet Kabba cloak with midnight onyx Kumeta and golden collar for elite imperial guards.",
            cape = CapeSkin.BLACK_VELVET_CABBA,
            kumeta = KumetaSkin.MIDNIGHT_ONYX_KUMETA,
            headgear = HeadgearSkin.IMPERIAL_GOLD_HELM,
            sash = SashSkin.GOLDEN_ORDER_CHAIN
        ),
        AttirePreset(
            id = "afar_danakil_nomad",
            name = "Afar Desert Ranger",
            amharicName = "የአፋር በረሃ ንስር",
            description = "Lightweight amber sand Netela with nomad headwrap and national tri-color sash suited for arid plains.",
            cape = CapeSkin.DESERT_NETELA,
            kumeta = KumetaSkin.PATRIOT_KHAKI_KUMETA,
            headgear = HeadgearSkin.DESERT_TAGELMUST,
            sash = SashSkin.TRI_COLOR_TIBEB
        )
    )

    fun equipPreset(preset: AttirePreset) {
        currentCustomization = currentCustomization.copy(
            cape = preset.cape,
            kumeta = preset.kumeta,
            headgear = preset.headgear,
            sash = preset.sash
        )
    }

    fun equipCape(cape: CapeSkin) {
        currentCustomization = currentCustomization.copy(cape = cape)
    }

    fun equipKumeta(kumeta: KumetaSkin) {
        currentCustomization = currentCustomization.copy(kumeta = kumeta)
    }

    fun equipHeadgear(headgear: HeadgearSkin) {
        currentCustomization = currentCustomization.copy(headgear = headgear)
    }

    fun equipSash(sash: SashSkin) {
        currentCustomization = currentCustomization.copy(sash = sash)
    }
}

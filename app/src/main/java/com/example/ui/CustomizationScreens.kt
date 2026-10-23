package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

enum class AttireTab(val label: String, val amharicLabel: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CAPES("Kuta & Cape", "ኩታና ካባ", Icons.Default.Shield),
    KUMETA("Kumeta", "ቁሜታ", Icons.Default.Accessibility),
    HEADGEAR("Headgear", "ጎፈሬ", Icons.Default.MilitaryTech),
    SASH("Jano Sash", "የደረት ድግ", Icons.Default.Bookmark),
    PRESETS("Full Outfits", "ሙሉ ልብስ", Icons.Default.AutoAwesome)
}

@Composable
fun WarriorCustomizationScreen(
    currentHero: WarriorHero,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedTab by remember { mutableStateOf(AttireTab.CAPES) }
    var activeCustomization by remember {
        mutableStateOf(WarriorCustomizationRepository.currentCustomization.copy())
    }
    var previewJetpack by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF060911))
                )
            )
    ) {
        val isCompact = maxWidth < 760.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isCompact) 12.dp else 24.dp, vertical = 12.dp)
        ) {
            // HEADER BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x40F59E0B), CircleShape)
                            .testTag("customization_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WARRIOR ATTIRE & SKINS",
                                color = Color(0xFFF59E0B),
                                fontSize = if (isCompact) 18.sp else 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Flag badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                            }
                        }
                        Text(
                            text = "የባህል አልባሳት - Equip authentic Jano, Kuta, Kumeta, & Gofere Attire",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Warrior Archetype Pill
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, currentHero.primaryColor, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(currentHero.primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentHero.amharicName.take(1),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = currentHero.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TibebPatternRibbon()
            Spacer(modifier = Modifier.height(10.dp))

            // MAIN CONTENT (Adaptive split pane)
            if (isCompact) {
                // Portrait Layout: Top character preview, bottom tabbed catalog
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Live Character Preview Card
                    WarriorPreviewCard(
                        hero = currentHero,
                        customization = activeCustomization,
                        isJetpacking = previewJetpack,
                        onToggleJetpack = { previewJetpack = !previewJetpack },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    )

                    // Attire Category Selector Tabs
                    AttireTabsRow(
                        selectedTab = selectedTab,
                        onSelectTab = { selectedTab = it }
                    )

                    // Attire Options List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        renderAttireItems(
                            tab = selectedTab,
                            customization = activeCustomization,
                            onUpdate = { updated ->
                                activeCustomization = updated
                                WarriorCustomizationRepository.currentCustomization = updated
                            }
                        )
                    }
                }
            } else {
                // Landscape Layout: Left character preview, Right tabbed catalog
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left: Live Character Preview & Summary
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WarriorPreviewCard(
                            hero = currentHero,
                            customization = activeCustomization,
                            isJetpacking = previewJetpack,
                            onToggleJetpack = { previewJetpack = !previewJetpack },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )

                        // Equipped Summary Card
                        EquippedSummaryCard(customization = activeCustomization)
                    }

                    // Right: Category Tabs & Attire Cards
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AttireTabsRow(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it }
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            renderAttireItems(
                                tab = selectedTab,
                                customization = activeCustomization,
                                onUpdate = { updated ->
                                    activeCustomization = updated
                                    WarriorCustomizationRepository.currentCustomization = updated
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttireTabsRow(
    selectedTab: AttireTab,
    onSelectTab: (AttireTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AttireTab.values()) { tab ->
            val isSelected = selectedTab == tab
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectTab(tab) }
                    .border(
                        1.5.dp,
                        if (isSelected) Color(0xFFF59E0B) else Color(0x33475569),
                        RoundedCornerShape(10.dp)
                    )
                    .testTag("attire_tab_${tab.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = tab.amharicLabel,
                            color = if (isSelected) Color(0xFFFDE68A) else Color(0xFF64748B),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WarriorPreviewCard(
    hero: WarriorHero,
    customization: WarriorCustomization,
    isJetpacking: Boolean,
    onToggleJetpack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFFD97706), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live Warrior Canvas Rendering
            WarriorAttireCanvasPreview(
                hero = hero,
                customization = customization,
                weapon = hero.initialWeapon,
                isJetpacking = isJetpacking,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )

            // Top-left Cape Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xD9000000))
                    .border(1.dp, customization.cape.tibebColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = customization.cape.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Top-right Thruster Toggle
            OutlinedButton(
                onClick = onToggleJetpack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isJetpacking) Color(0xFFFF6D00) else Color(0x6694A3B8)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isJetpacking) Color(0x33FF6D00) else Color(0x990F172A)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = if (isJetpacking) Color(0xFFFF6D00) else Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isJetpacking) "THRUST ON" else "JETPACK TEST",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom Ribbon of Active Swatch Colors
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xCC090D16))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${customization.cape.amharicName} • ${customization.kumeta.amharicName}",
                    color = Color(0xFFFDE68A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(customization.cape.capeColor).border(0.5.dp, Color.White, CircleShape))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(customization.cape.tibebColor).border(0.5.dp, Color.White, CircleShape))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(customization.kumeta.trouserColor).border(0.5.dp, Color.White, CircleShape))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(customization.sash.bandColor).border(0.5.dp, Color.White, CircleShape))
                }
            }
        }
    }
}

@Composable
fun EquippedSummaryCard(customization: WarriorCustomization, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33475569))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "EQUIPPED ATTIRE (የለበሱት አልባሳት)",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            SummaryRow(label = "Cape/Kuta:", value = customization.cape.title)
            SummaryRow(label = "Kumeta:", value = customization.kumeta.title)
            SummaryRow(label = "Headgear:", value = customization.headgear.title)
            SummaryRow(label = "Jano Sash:", value = customization.sash.title)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Renders attire card items based on active tab
 */
private fun androidx.compose.foundation.lazy.LazyListScope.renderAttireItems(
    tab: AttireTab,
    customization: WarriorCustomization,
    onUpdate: (WarriorCustomization) -> Unit
) {
    when (tab) {
        AttireTab.CAPES -> {
            items(CapeSkin.values()) { skin ->
                val isEquipped = customization.cape == skin
                AttireOptionCard(
                    title = skin.title,
                    amharicTitle = skin.amharicName,
                    description = skin.description,
                    badge = skin.categoryBadge,
                    swatchColors = listOf(skin.capeColor, skin.tibebColor, skin.trimColor),
                    isEquipped = isEquipped,
                    onEquip = { onUpdate(customization.copy(cape = skin)) }
                )
            }
        }
        AttireTab.KUMETA -> {
            items(KumetaSkin.values()) { skin ->
                val isEquipped = customization.kumeta == skin
                AttireOptionCard(
                    title = skin.title,
                    amharicTitle = skin.amharicName,
                    description = skin.description,
                    badge = "Riding Attire",
                    swatchColors = listOf(skin.trouserColor, skin.fringeColor, skin.bootColor),
                    isEquipped = isEquipped,
                    onEquip = { onUpdate(customization.copy(kumeta = skin)) }
                )
            }
        }
        AttireTab.HEADGEAR -> {
            items(HeadgearSkin.values()) { skin ->
                val isEquipped = customization.headgear == skin
                AttireOptionCard(
                    title = skin.title,
                    amharicTitle = skin.amharicName,
                    description = skin.description,
                    badge = "Headdress",
                    swatchColors = listOf(skin.primaryColor, skin.accentColor),
                    isEquipped = isEquipped,
                    onEquip = { onUpdate(customization.copy(headgear = skin)) }
                )
            }
        }
        AttireTab.SASH -> {
            items(SashSkin.values()) { skin ->
                val isEquipped = customization.sash == skin
                AttireOptionCard(
                    title = skin.title,
                    amharicTitle = skin.amharicName,
                    description = skin.description,
                    badge = "Chest Regalia",
                    swatchColors = listOf(skin.bandColor, skin.stripeColor),
                    isEquipped = isEquipped,
                    onEquip = { onUpdate(customization.copy(sash = skin)) }
                )
            }
        }
        AttireTab.PRESETS -> {
            items(WarriorCustomizationRepository.presets) { preset ->
                val isEquipped = customization.cape == preset.cape &&
                        customization.kumeta == preset.kumeta &&
                        customization.headgear == preset.headgear &&
                        customization.sash == preset.sash

                AttireOptionCard(
                    title = preset.name,
                    amharicTitle = preset.amharicName,
                    description = preset.description,
                    badge = "Full Regalia",
                    swatchColors = listOf(preset.cape.capeColor, preset.cape.tibebColor, preset.kumeta.trouserColor, preset.headgear.primaryColor),
                    isEquipped = isEquipped,
                    onEquip = {
                        onUpdate(
                            customization.copy(
                                cape = preset.cape,
                                kumeta = preset.kumeta,
                                headgear = preset.headgear,
                                sash = preset.sash
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AttireOptionCard(
    title: String,
    amharicTitle: String,
    description: String,
    badge: String,
    swatchColors: List<Color>,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onEquip() }
            .border(
                if (isEquipped) 2.dp else 1.dp,
                if (isEquipped) Color(0xFFF59E0B) else Color(0x33475569),
                RoundedCornerShape(12.dp)
            )
            .testTag("attire_card_${title.lowercase().replace(" ", "_").take(15)}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color(0xFF1E293B) else Color(0xFF111827)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = badge, color = Color(0xFFFDE68A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = amharicTitle,
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Color swatches
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Palette:", color = Color(0xFF64748B), fontSize = 9.sp)
                    swatchColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(1.dp, Color(0x66FFFFFF), CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action: Equipped vs Equip Button
            if (isEquipped) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFD97706))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text(text = "EQUIPPED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onEquip,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(text = "EQUIP (ልበስ)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

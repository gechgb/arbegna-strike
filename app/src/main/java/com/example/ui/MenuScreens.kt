package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.example.lan.NetworkManager
import com.example.model.*

@Composable
fun MainMenuScreen(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    selectedHero: WarriorHero,
    selectedArena: MapArena,
    onHeroClick: () -> Unit,
    onArmoryClick: () -> Unit,
    onArenaSelect: (MapArena) -> Unit,
    onStartBotMatch: (Int) -> Unit,
    onStartSurvival: () -> Unit,
    onLanMultiplayerClick: () -> Unit,
    onPracticeRangeClick: () -> Unit,
    onCampaignClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var botCount by remember { mutableStateOf(3) }
    var showArenaDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF090D16))
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        val isCompactWidth = maxWidth < 680.dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = if (isCompactWidth) 14.dp else 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HEADER BAR: Title & Profile
            if (isCompactWidth) {
                // Stacked Title + Profile for portrait / narrow phones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFD97706))
                                    .border(2.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Emblem",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ARBEGNA STRIKE",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
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
                                    text = "አርበኛ ስትራይክ - 2D Action Combat",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Hero Avatar Quick Button
                        Button(
                            onClick = onHeroClick,
                            colors = ButtonDefaults.buttonColors(containerColor = selectedHero.primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("hero_barracks_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = selectedHero.amharicName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hero ▾",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Profile input bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Warrior:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = playerName,
                            onValueChange = onPlayerNameChange,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .padding(horizontal = 8.dp)
                                .testTag("player_name_input"),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF59E0B),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )
                    }
                }
            } else {
                // Wide / Landscape Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD97706))
                                .border(2.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Emblem",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ARBEGNA STRIKE",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFDC2626)))
                                }
                            }
                            Text(
                                text = "አርበኛ ስትራይክ - Ethiopian 2D LAN Action Combat",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Player Profile Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.border(1.dp, Color(0x33F59E0B), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Warrior Name",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                )
                                OutlinedTextField(
                                    value = playerName,
                                    onValueChange = onPlayerNameChange,
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(44.dp)
                                        .testTag("player_name_input"),
                                    textStyle = LocalTextStyle.current.copy(
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFF59E0B),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    )
                                )
                            }

                            Button(
                                onClick = onHeroClick,
                                colors = ButtonDefaults.buttonColors(containerColor = selectedHero.primaryColor),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("hero_barracks_button")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = selectedHero.amharicName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Change Hero",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // PRIMARY HERO QUICK MATCH BUTTON - GUARANTEED 1-TAP INSTANT LAUNCH!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartBotMatch(botCount) }
                    .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(14.dp))
                    .testTag("quick_play_hero_banner"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB45309)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706))
                                .border(1.5.dp, Color(0xFFFDE68A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "QUICK MATCH • ፈጣን ፍልሚያ ጀምር",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Instant 3-Bot Deathmatch • ${selectedHero.title} • Map: ${selectedArena.title}",
                                color = Color(0xFFFDE68A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onStartBotMatch(botCount) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("quick_play_button")
                    ) {
                        Text(
                            text = "START NOW",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // MIDDLE SECTION: Game Mode Cards (Responsive Column in portrait, Row in landscape)
            if (isCompactWidth) {
                // Stack cards vertically for comfortable touch targets and zero clipping
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 0: HISTORICAL CAMPAIGN
                    ModeCard(
                        title = "Historical Campaign",
                        amharicTitle = "የኢትዮጵያ ታሪካዊ ዘመቻዎች (Campaign)",
                        description = "Relive Ethiopia's legendary battles: Gundet 1875, Dogali 1887, Adwa 1896, and Arbegna 1941 with 3-star objectives!",
                        badge = "Story Missions",
                        color = Color(0xFFF59E0B),
                        icon = Icons.Default.MilitaryTech,
                        onClick = onCampaignClick,
                        testTag = "start_campaign_card"
                    ) {
                        Button(
                            onClick = onCampaignClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("open_campaign_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "STORY CAMPAIGN (ታሪካዊ ዘመቻ)", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    // Card 0B: HEROIC CHALLENGES
                    ModeCard(
                        title = "Heroic Challenges",
                        amharicTitle = "የጀግንነት ፈተናዎች (Tactical Trials)",
                        description = "Special combat trials with unique weapon restrictions: Shotel duels, Simien ridge snipers, and turbo jetpacks!",
                        badge = "Special Modifiers",
                        color = Color(0xFF38BDF8),
                        icon = Icons.Default.Bolt,
                        onClick = onChallengesClick,
                        testTag = "start_challenges_card"
                    ) {
                        Button(
                            onClick = onChallengesClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("open_challenges_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "HEROIC CHALLENGES (ፈተናዎች)", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    // Card 1: BOT DEATHMATCH
                    ModeCard(
                        title = "Bot Deathmatch",
                        amharicTitle = "የቦት ፍልሚያ (Mini Militia FFA)",
                        description = "Free-for-all deathmatch with smart Ethiopian AI warriors equipped with jetpacks, rifles, and bows.",
                        badge = "Solo Action",
                        color = Color(0xFFD97706),
                        icon = Icons.Default.SportsEsports,
                        onClick = { onStartBotMatch(botCount) },
                        testTag = "start_bot_match_card"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bots: $botCount",
                                color = Color(0xFFFDE68A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (1..5).forEach { c ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (botCount == c) Color(0xFFF59E0B) else Color(0xFF334155))
                                            .clickable { botCount = c },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "$c", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onStartBotMatch(botCount) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("play_bot_match_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "PLAY MATCH (ይጫወቱ)", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    // Card 2: FORTRESS SURVIVAL
                    ModeCard(
                        title = "Fortress Survival",
                        amharicTitle = "የምሽግ መከላከያ (Wave Defense)",
                        description = "Endure relentless waves of enemy invaders atop the Simien battlements. Weapons upgrade each wave!",
                        badge = "Wave Defense",
                        color = Color(0xFFDC2626),
                        icon = Icons.Default.Shield,
                        onClick = onStartSurvival,
                        testTag = "survival_mode_card"
                    ) {
                        Button(
                            onClick = onStartSurvival,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_survival_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "SURVIVE WAVES (መከላከያ)", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    // Card 3: LAN MULTIPLAYER
                    ModeCard(
                        title = "LAN Multiplayer",
                        amharicTitle = "የአካባቢ መረብ (Wi-Fi / Hotspot)",
                        description = "Connect over Wi-Fi or Mobile Hotspot! Host matches for nearby friends or join with 1-click.",
                        badge = "Wi-Fi / Hotspot",
                        color = Color(0xFF10B981),
                        icon = Icons.Default.Wifi,
                        onClick = onLanMultiplayerClick,
                        testTag = "lan_multiplayer_card"
                    ) {
                        Button(
                            onClick = onLanMultiplayerClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("open_lan_lobby_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "HOST / JOIN LAN (መረብ ፍልሚያ)", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // Wide / Landscape Layout: 2 structured rows for comfortable spacing
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Row 1: Story Campaign & Heroic Challenges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 0: HISTORICAL CAMPAIGN
                        ModeCard(
                            title = "Story Campaign",
                            amharicTitle = "ታሪካዊ ዘመቻ",
                            description = "Relive Ethiopia's legendary battles: Gundet, Dogali, Adwa, and Arbegna with 3-star objectives!",
                            badge = "Story Missions",
                            color = Color(0xFFF59E0B),
                            icon = Icons.Default.MilitaryTech,
                            modifier = Modifier.weight(1f),
                            onClick = onCampaignClick,
                            testTag = "start_campaign_card"
                        ) {
                            Button(
                                onClick = onCampaignClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("open_campaign_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "CAMPAIGN (ታሪካዊ ዘመቻ)", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }

                        // Card 0B: HEROIC CHALLENGES
                        ModeCard(
                            title = "Heroic Challenges",
                            amharicTitle = "የጀግንነት ፈተናዎች",
                            description = "Special combat trials with unique weapon restrictions: Shotel duels, Simien ridge snipers, and turbo jetpacks!",
                            badge = "Special Modifiers",
                            color = Color(0xFF38BDF8),
                            icon = Icons.Default.Bolt,
                            modifier = Modifier.weight(1f),
                            onClick = onChallengesClick,
                            testTag = "start_challenges_card"
                        ) {
                            Button(
                                onClick = onChallengesClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("open_challenges_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "HEROIC CHALLENGES (ፈተናዎች)", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }

                    // Row 2: Multiplayer & Solo Deathmatch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 1: BOT DEATHMATCH
                        ModeCard(
                            title = "Bot Deathmatch",
                            amharicTitle = "የቦት ፍልሚያ",
                            description = "Fast-paced free-for-all deathmatch with smart Ethiopian AI warriors with jetpacks, rifles, and bows.",
                            badge = "Solo Action",
                            color = Color(0xFFD97706),
                            icon = Icons.Default.SportsEsports,
                            modifier = Modifier.weight(1f),
                            onClick = { onStartBotMatch(botCount) },
                            testTag = "start_bot_match_card"
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Bots: $botCount", color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (1..5).forEach { c ->
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(if (botCount == c) Color(0xFFF59E0B) else Color(0xFF334155))
                                                    .clickable { botCount = c },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "$c", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onStartBotMatch(botCount) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("play_bot_match_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "PLAY MATCH", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        }

                        // Card 2: LAN MULTIPLAYER
                        ModeCard(
                            title = "LAN Multiplayer",
                            amharicTitle = "የአካባቢ መረብ",
                            description = "Connect over Wi-Fi or Mobile Hotspot! Host matches for nearby friends or join with 1-click.",
                            badge = "Wi-Fi / Hotspot",
                            color = Color(0xFF10B981),
                            icon = Icons.Default.Wifi,
                            modifier = Modifier.weight(1f),
                            onClick = onLanMultiplayerClick,
                            testTag = "lan_multiplayer_card"
                        ) {
                            Button(
                                onClick = onLanMultiplayerClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("open_lan_lobby_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "HOST / JOIN LAN", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }

                        // Card 3: FORTRESS SURVIVAL
                        ModeCard(
                            title = "Fortress Survival",
                            amharicTitle = "የምሽግ መከላከያ",
                            description = "Endure relentless waves of enemy invaders atop the Simien battlements. Weapons upgrade each wave!",
                            badge = "Wave Defense",
                            color = Color(0xFFDC2626),
                            icon = Icons.Default.Shield,
                            modifier = Modifier.weight(1f),
                            onClick = onStartSurvival,
                            testTag = "survival_mode_card"
                        ) {
                            Button(
                                onClick = onStartSurvival,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("start_survival_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "SURVIVE WAVES", fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // BOTTOM BAR: Arena Picker, Armory, Target Practice
            if (isCompactWidth) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF111827))
                        .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Arena Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showArenaDialog = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Landscape, contentDescription = "Arena", tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Arena Map: ${selectedArena.title}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${selectedArena.amharicName} (Tap to change)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Armory Button
                        OutlinedButton(
                            onClick = onArmoryClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDE68A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("armory_screen_button")
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = "Armory", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Armory", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Target Range
                        OutlinedButton(
                            onClick = onPracticeRangeClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("practice_range_button")
                        ) {
                            Icon(Icons.Default.TrackChanges, contentDescription = "Target", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Range", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF111827))
                        .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Arena Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showArenaDialog = true }
                    ) {
                        Icon(Icons.Default.Landscape, contentDescription = "Arena", tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Arena Map: ${selectedArena.title}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = selectedArena.amharicName, color = Color(0xFF94A3B8), fontSize = 10.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Armory Button
                        OutlinedButton(
                            onClick = onArmoryClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDE68A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("armory_screen_button")
                        ) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = "Armory", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Armory (10 Weapons)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Target Range
                        OutlinedButton(
                            onClick = onPracticeRangeClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("practice_range_button")
                        ) {
                            Icon(Icons.Default.TrackChanges, contentDescription = "Target", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Shooting Range", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Arena Selection Dialog
        if (showArenaDialog) {
            AlertDialog(
                onDismissRequest = { showArenaDialog = false },
                title = { Text("Choose Battlefield Arena", color = Color(0xFFF59E0B), fontWeight = FontWeight.Black) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MapArena.values().forEach { arena ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onArenaSelect(arena)
                                        showArenaDialog = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedArena == arena) Color(0xFF374151) else Color(0xFF1E293B)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedArena == arena) Color(0xFFF59E0B) else Color.Transparent
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = arena.title, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(text = arena.amharicName, color = Color(0xFFFDE68A), fontSize = 11.sp)
                                    Text(text = arena.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showArenaDialog = false }) {
                        Text("Close", color = Color.White)
                    }
                },
                containerColor = Color(0xFF111827)
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    amharicTitle: String,
    description: String,
    badge: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .testTag(testTag)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = badge, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(text = amharicTitle, color = Color(0xFFFDE68A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = description, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
            content()
        }
    }
}

@Composable
fun LanMultiplayerScreen(
    networkManager: NetworkManager,
    playerName: String,
    selectedHero: WarriorHero,
    selectedArena: MapArena,
    onArenaSelect: (MapArena) -> Unit,
    onLaunchMatch: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHostingTab by remember { mutableStateOf(true) }
    var hostIpInput by remember { mutableStateOf(NetworkManager.HOTSPOT_DEFAULT_IP) }
    var connectionStatus by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var scanRevision by remember { mutableIntStateOf(0) }
    val deviceIp = remember { NetworkManager.getDeviceIpAddress() }

    LaunchedEffect(networkManager) {
        networkManager.onDiscoveredHostsUpdated = {
            scanRevision++
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "LAN MULTIPLAYER (የአካባቢ መረብ)",
                            color = Color(0xFF10B981),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Wi-Fi & Mobile Hotspot Local Combat",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = {
                            isHostingTab = true
                            networkManager.startHost(playerName, selectedHero, selectedArena)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHostingTab) Color(0xFF10B981) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Host Match", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { isHostingTab = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isHostingTab) Color(0xFF10B981) else Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Join Match", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Area
            if (isHostingTab) {
                // HOST MATCH VIEW
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Host Details & Connection info
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Device Host IP:",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Text(
                                text = deviceIp,
                                color = Color(0xFFF59E0B),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "How to Play with Friends:",
                                        color = Color(0xFF10B981),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "1. Turn ON your Mobile Hotspot (or connect all phones to the same Wi-Fi).\n2. Tell friends to open Arbegna Strike -> LAN Multiplayer -> Join Match.\n3. They can tap 'Quick Join Hotspot (192.168.43.1)' or enter '$deviceIp'.",
                                        color = Color(0xFFD1D5DB),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Selected Map: ${selectedArena.title}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = onLaunchMatch,
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("launch_lan_match_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "LAUNCH MATCH (${networkManager.lobbyPlayers.size} Warriors)", fontWeight = FontWeight.Black, fontSize = 14.sp)
                            }
                        }
                    }

                    // Right Column: Connected Warriors Lobby List
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Lobby Warriors (${networkManager.lobbyPlayers.size}/6):",
                                color = Color(0xFFF59E0B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(networkManager.lobbyPlayers.toList()) { p ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(28.dp).clip(CircleShape).background(p.hero.primaryColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = p.hero.amharicName.take(1), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(text = p.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(text = p.hero.title, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            }
                                        }
                                        Text(
                                            text = if (p.isHost) "HOST" else "READY",
                                            color = if (p.isHost) Color(0xFFF59E0B) else Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // JOIN MATCH VIEW
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Auto-Discovery Scanner
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "AUTO-DISCOVER MATCHES",
                                        color = Color(0xFF10B981),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Scan local Wi-Fi / Hotspot broadcast",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        isScanning = true
                                        networkManager.scanLanHosts {
                                            isScanning = false
                                            scanRevision++
                                        }
                                    },
                                    enabled = !isScanning,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    if (isScanning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scanning...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scan LAN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Show list of discovered hosts
                            val hosts = remember(scanRevision, networkManager.discoveredHosts.size) {
                                networkManager.discoveredHosts.toList()
                            }

                            if (hosts.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isScanning) "Searching for active hosts on local network..."
                                        else "No active hosts discovered yet.\n• Ensure friends are on the same Wi-Fi or Hotspot\n• Ensure Host opened 'Host Match'\n• Tap 'Scan LAN' or connect directly on the right.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(hosts) { host ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF1E293B))
                                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${host.hostName}'s Match",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Map: ${host.arenaTitle} • ${host.playerCount} Warriors",
                                                    color = Color(0xFFFDE68A),
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = "IP: ${host.ip}",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    isConnecting = true
                                                    connectionStatus = "Connecting to ${host.hostName} (${host.ip})..."
                                                    networkManager.joinHost(host.ip, playerName, selectedHero) { success ->
                                                        isConnecting = false
                                                        connectionStatus = if (success) "Connected to ${host.hostName}! Waiting for Host to start match..." else "Failed to connect to ${host.ip}."
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("JOIN", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Right Column: Direct Hotspot / Custom IP & Status
                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "DIRECT / HOTSPOT CONNECTION",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Join Hotspot Button
                                Button(
                                    onClick = {
                                        isConnecting = true
                                        connectionStatus = "Connecting to standard Hotspot Gateway..."
                                        networkManager.joinHost(NetworkManager.HOTSPOT_DEFAULT_IP, playerName, selectedHero) { success ->
                                            isConnecting = false
                                            connectionStatus = if (success) "Connected to Host! Waiting for Host to start match..." else "Connection failed. Check Host Wi-Fi/Hotspot."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Icon(Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Quick Join Hotspot (192.168.43.1)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "— OR SPECIFY HOST IP —", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = hostIpInput,
                                        onValueChange = { hostIpInput = it },
                                        label = { Text("Host IP", fontSize = 10.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    )
                                    Button(
                                        onClick = {
                                            isConnecting = true
                                            connectionStatus = "Connecting to $hostIpInput..."
                                            networkManager.joinHost(hostIpInput, playerName, selectedHero) { success ->
                                                isConnecting = false
                                                connectionStatus = if (success) "Connected to Host! Waiting for Host to start match..." else "Could not reach $hostIpInput."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text(text = "CONNECT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }
                            }

                            Column {
                                if (connectionStatus.isNotEmpty()) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (networkManager.isConnected) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (networkManager.isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (networkManager.isConnected) Color(0xFF34D399) else Color(0xFFF87171),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = connectionStatus,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (networkManager.isConnected) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Joined Lobby with ${networkManager.lobbyPlayers.size} Warriors! Match will start automatically when Host taps Launch.",
                                        color = Color(0xFFFDE68A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArmoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weapons = WeaponType.values()
    var selectedWeapon by remember { mutableStateOf(weapons.first()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ETHIOPIAN ARSENAL & ARMORY",
                            color = Color(0xFFF59E0B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "From Ancient Sparrow Bow & Arrow up to Modern Rifles & RPGs",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Split View: Weapons List on Left, Weapon Detail on Right
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Weapons List
                LazyColumn(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(weapons) { w ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedWeapon = w },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedWeapon == w) Color(0xFF1E293B) else Color(0xFF111827)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selectedWeapon == w) w.color else Color(0x33475569)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(w.color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "T${w.tier}",
                                            color = w.color,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = w.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = w.amharicName, color = w.color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Text(
                                    text = w.category,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Weapon Detail Card
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, selectedWeapon.color)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tier ${selectedWeapon.tier} Weapon",
                                    color = selectedWeapon.color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedWeapon.category,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedWeapon.title,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = selectedWeapon.amharicName,
                                color = Color(0xFFFDE68A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = selectedWeapon.description,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats Bars
                            StatBar("Damage", (selectedWeapon.damage / 140f).coerceIn(0f, 1f), "${selectedWeapon.damage}", Color(0xFFEF4444))
                            StatBar("Fire Rate", (1f - (selectedWeapon.fireRateMs / 1500f)).coerceIn(0.1f, 1f), "${1000 / selectedWeapon.fireRateMs} /s", Color(0xFFF59E0B))
                            StatBar("Magazine", (selectedWeapon.magazineSize / 35f).coerceIn(0.05f, 1f), "${selectedWeapon.magazineSize}", Color(0xFF10B981))
                            StatBar("Velocity", (selectedWeapon.projectileSpeed / 45f).coerceIn(0.1f, 1f), "${selectedWeapon.projectileSpeed.toInt()} m/s", Color(0xFF38BDF8))
                        }

                        // Feature badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (selectedWeapon.isExplosive) {
                                BadgeItem("Explosive Splash", Color(0xFFFF6D00))
                            }
                            if (selectedWeapon.gravityEffect > 0.2f) {
                                BadgeItem("Ballistic Arc", Color(0xFFD97706))
                            }
                            if (selectedWeapon.pellets > 1) {
                                BadgeItem("Buckshot Spread", Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBar(label: String, ratio: Float, valueStr: String, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text(text = valueStr, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun BadgeItem(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HeroBarracksScreen(
    currentHero: WarriorHero,
    onSelectHero: (WarriorHero) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroes = WarriorHero.values()
    var selected by remember { mutableStateOf(currentHero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "HERO BARRACKS (ጀግኖች)", color = Color(0xFFF59E0B), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(text = "Choose your Ethiopian warrior legend", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heroes Grid/List
                LazyColumn(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(heroes) { h ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = h },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected == h) Color(0xFF1E293B) else Color(0xFF111827)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selected == h) h.primaryColor else Color(0x33475569)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(h.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = h.amharicName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = h.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = h.perkDescription, color = Color(0xFFFDE68A), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Selected Hero Showcase Card
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, selected.primaryColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(selected.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selected.amharicName.take(1), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = selected.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text(text = selected.amharicName, color = Color(0xFFFDE68A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = selected.description, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Perk Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Warrior Perk:", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = selected.perkDescription, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Starting Weapon: ${selected.initialWeapon.amharicName}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onSelectHero(selected)
                                onBackClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = selected.primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text(text = "SELECT WARRIOR", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

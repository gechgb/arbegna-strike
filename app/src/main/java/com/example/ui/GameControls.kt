package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GameEngine
import com.example.model.GameMode
import com.example.model.WarriorHero
import com.example.model.WeaponType
import kotlin.math.*

@Composable
fun GameHudAndControls(
    engine: GameEngine,
    onMoveChange: (Float, Float) -> Unit,
    onAimChange: (Float, Float) -> Unit,
    onFireChange: (Boolean) -> Unit,
    onJetpackChange: (Boolean) -> Unit = {},
    isFireHeld: Boolean = false,
    isJetpackHeld: Boolean = false,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localPlayer = engine.localPlayer
    var showTauntMenu by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 640.dp

        // TOP HUD BAR
        TopHudBar(
            engine = engine,
            onPauseClick = onPauseClick,
            onTauntClick = { showTauntMenu = !showTauntMenu },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Kill Feed Overlay (Top Right below top bar)
        KillFeedOverlay(
            engine = engine,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 16.dp)
        )

        // Range Weapon Selector (In Weapon Range Mode)
        if (engine.mode == GameMode.WEAPON_RANGE) {
            RangeWeaponSelector(
                engine = engine,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 70.dp, start = 16.dp)
            )
        }

        // Wave Announcement Banner (In Survival Mode)
        if (engine.waveBannerMessage.isNotEmpty()) {
            WaveBanner(
                message = engine.waveBannerMessage,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        // Quick Taunt Dialog / Drawer
        if (showTauntMenu) {
            TauntSelectorDialog(
                onSelectTaunt = { text ->
                    showTauntMenu = false
                    engine.audio.playSound("fanfare")
                    engine.networkManager?.broadcastTaunt(localPlayer.name, text)
                },
                onDismiss = { showTauntMenu = false },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // BOTTOM CONTROLS: Responsive layout for both portrait and landscape
        if (isNarrow) {
            // Action Buttons centered above joysticks
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Melee Shotel Slash
                ActionButton(
                    title = "Slash",
                    subtitle = "ሾተል",
                    icon = Icons.Default.FlashOn,
                    badge = if (localPlayer.meleeCooldownMs > 0) "${localPlayer.meleeCooldownMs / 1000 + 1}s" else "READY",
                    color = Color(0xFFEF4444),
                    enabled = localPlayer.meleeCooldownMs <= 0,
                    onClick = { engine.executeMeleeSlash(localPlayer) },
                    testTag = "melee_slash_button"
                )

                // Switch Weapon (Primary <-> Secondary)
                ActionButton(
                    title = "Switch",
                    subtitle = localPlayer.secondaryWeapon.amharicName,
                    icon = Icons.Default.SwapHoriz,
                    badge = "T${localPlayer.secondaryWeapon.tier}",
                    color = Color(0xFFF59E0B),
                    enabled = true,
                    onClick = { engine.switchWeapon(localPlayer) },
                    testTag = "switch_weapon_button"
                )

                // Reload
                ActionButton(
                    title = "Reload",
                    subtitle = if (localPlayer.isReloading) "..." else "${localPlayer.ammoInMag}/${localPlayer.reserveAmmo}",
                    icon = Icons.Default.Refresh,
                    badge = if (localPlayer.isReloading) "..." else "MAG",
                    color = Color(0xFF10B981),
                    enabled = !localPlayer.isReloading && localPlayer.ammoInMag < localPlayer.currentWeapon.magazineSize,
                    onClick = { engine.startReload(localPlayer) },
                    testTag = "reload_button"
                )
            }

            // Left Cluster: Movement Joystick + Dedicated Jetpack Button
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VirtualJoystick(
                    label = "Move",
                    accentColor = Color(0xFFFF6D00),
                    onValueChange = onMoveChange,
                    modifier = Modifier.testTag("left_movement_joystick")
                )
                JetpackFlyButton(
                    isJetpackHeld = isJetpackHeld,
                    fuelRatio = (localPlayer.jetpackFuel / localPlayer.maxJetpackFuel).coerceIn(0f, 1f),
                    onJetpackChange = onJetpackChange
                )
            }

            // Right Cluster: Primary FIRE Button + 360 Aim Joystick
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PrimaryFireButton(
                    isFireHeld = isFireHeld,
                    onFireChange = onFireChange
                )
                RightAimJoystick(
                    onAimChange = onAimChange,
                    onFireChange = onFireChange,
                    modifier = Modifier.testTag("right_aim_joystick")
                )
            }
        } else {
            // Landscape Spacious Controls Row
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // LEFT: Virtual Joystick + Dedicated Jetpack Button
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VirtualJoystick(
                        label = "Move & Fly",
                        accentColor = Color(0xFFFF6D00),
                        onValueChange = onMoveChange,
                        modifier = Modifier.testTag("left_movement_joystick")
                    )
                    JetpackFlyButton(
                        isJetpackHeld = isJetpackHeld,
                        fuelRatio = (localPlayer.jetpackFuel / localPlayer.maxJetpackFuel).coerceIn(0f, 1f),
                        onJetpackChange = onJetpackChange
                    )
                }

                // CENTER ACTION BUTTONS: Melee Shotel, Switch Weapon, Reload
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // Melee Shotel Slash
                    ActionButton(
                        title = "Shotel Slash",
                        subtitle = "ሾተል",
                        icon = Icons.Default.FlashOn,
                        badge = if (localPlayer.meleeCooldownMs > 0) "${localPlayer.meleeCooldownMs / 1000 + 1}s" else "READY",
                        color = Color(0xFFEF4444),
                        enabled = localPlayer.meleeCooldownMs <= 0,
                        onClick = { engine.executeMeleeSlash(localPlayer) },
                        testTag = "melee_slash_button"
                    )

                    // Switch Weapon (Primary <-> Secondary)
                    ActionButton(
                        title = "Switch",
                        subtitle = localPlayer.secondaryWeapon.amharicName,
                        icon = Icons.Default.SwapHoriz,
                        badge = "Tier ${localPlayer.secondaryWeapon.tier}",
                        color = Color(0xFFF59E0B),
                        enabled = true,
                        onClick = { engine.switchWeapon(localPlayer) },
                        testTag = "switch_weapon_button"
                    )

                    // Reload
                    ActionButton(
                        title = "Reload",
                        subtitle = if (localPlayer.isReloading) "Loading..." else "${localPlayer.ammoInMag}/${localPlayer.reserveAmmo}",
                        icon = Icons.Default.Refresh,
                        badge = if (localPlayer.isReloading) "..." else "MAG",
                        color = Color(0xFF10B981),
                        enabled = !localPlayer.isReloading && localPlayer.ammoInMag < localPlayer.currentWeapon.magazineSize,
                        onClick = { engine.startReload(localPlayer) },
                        testTag = "reload_button"
                    )
                }

                // RIGHT: Primary FIRE Button + 360 Aim Joystick
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryFireButton(
                        isFireHeld = isFireHeld,
                        onFireChange = onFireChange
                    )
                    RightAimJoystick(
                        onAimChange = onAimChange,
                        onFireChange = onFireChange,
                        modifier = Modifier.testTag("right_aim_joystick")
                    )
                }
            }
        }

        // Respawn Countdown Overlay
        if (localPlayer.isDead) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ELIMINATED",
                        color = Color(0xFFEF4444),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Respawning in ${(localPlayer.respawnTimerMs / 1000) + 1}s...",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SafeProgressBar(
    ratio: Float,
    color: Color,
    trackColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    brush: Brush? = null
) {
    val safeRatio = if (ratio.isNaN() || ratio.isInfinite()) 0f else ratio.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2f))
            .background(trackColor)
    ) {
        if (safeRatio > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = safeRatio)
                    .clip(RoundedCornerShape(height / 2f))
                    .then(
                        if (brush != null) Modifier.background(brush)
                        else Modifier.background(color)
                    )
            )
        }
    }
}

@Composable
fun TopHudBar(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    onTauntClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = engine.localPlayer

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xEB0A0F1D))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.7f),
                        Color(0xFF10B981).copy(alpha = 0.5f),
                        Color(0xFFEF4444).copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        val isPortrait = maxWidth < 600.dp

        if (isPortrait) {
            // PORTRAIT COMPACT HUD: Two rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Row 1: Vitals left, Weapon & Controls right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Avatar + Bars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(player.hero.primaryColor, Color(0xFF0F172A))
                                    )
                                )
                                .border(1.5.dp, Color(0xFFFDE68A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = player.hero.amharicName.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Column(modifier = Modifier.width(120.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = player.name.take(10),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${player.kills}K/${player.deaths}D",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val hpRatio = if (player.maxHealth > 0) player.health.toFloat() / player.maxHealth else 0f
                            val hpBrush = if (hpRatio > 0.4f) {
                                Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444)))
                            }
                            SafeProgressBar(
                                ratio = hpRatio,
                                color = if (hpRatio > 0.4f) Color(0xFF10B981) else Color(0xFFEF4444),
                                trackColor = Color(0xFF1E293B),
                                height = 5.dp,
                                brush = hpBrush
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val fuelRatio = if (player.maxJetpackFuel > 0f) player.jetpackFuel / player.maxJetpackFuel else 0f
                            val fuelBrush = if (player.superJetpackTimerMs > 0L) {
                                Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFF59E0B)))
                            }
                            SafeProgressBar(
                                ratio = fuelRatio,
                                color = if (fuelRatio < 0.2f) Color(0xFFEF4444) else Color(0xFFFF6D00),
                                trackColor = Color(0xFF0F172A),
                                height = 4.dp,
                                brush = fuelBrush
                            )
                        }
                    }

                    // Right: Weapon ammo badge + buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.border(1.dp, player.currentWeapon.color, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = player.currentWeapon.amharicName,
                                    color = player.currentWeapon.color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (player.isReloading) "RELOAD" else "${player.ammoInMag}/${player.reserveAmmo}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = onTauntClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF374151))
                                .testTag("quick_taunt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Quick Taunt",
                                tint = Color(0xFFFDE68A),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onPauseClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF374151))
                                .testTag("pause_game_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause Game",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Row 2: Status / Objective banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33000000))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val m = engine.matchTimerSec.toInt() / 60
                    val s = engine.matchTimerSec.toInt() % 60
                    val timeStr = "${if (m < 10) "0" else ""}$m:${if (s < 10) "0" else ""}$s"

                    when (engine.mode) {
                        GameMode.HEROIC_CHALLENGE -> {
                            val ch = engine.currentChallenge
                            Text(
                                text = "TRIAL: ${ch?.title ?: "Challenge"}",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kills: ${player.kills}/${engine.campaignTargetKills} • $timeStr",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        GameMode.CAMPAIGN_MISSION -> {
                            val ch = engine.currentCampaignChapter
                            Text(
                                text = "BATTLE: ${ch?.dateText ?: "1896"} • ${ch?.title ?: "Adwa"}",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kills: ${player.kills}/${engine.campaignTargetKills} • $timeStr",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        GameMode.SURVIVAL_WAVES -> {
                            Text(
                                text = "WAVE ${engine.currentWave} • ${engine.players.count { it.isBot && !it.isDead }} Invaders Left",
                                color = Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = timeStr, color = Color.White, fontSize = 11.sp)
                        }
                        GameMode.WEAPON_RANGE -> {
                            Text(
                                text = "WEAPONS TRAINING (ልምምድ)",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Infinite Ammo", color = Color(0xFFFDE68A), fontSize = 10.sp)
                        }
                        else -> {
                            Text(
                                text = "${engine.currentArena.title} • Target: 15 Kills",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = timeStr, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // LANDSCAPE PANORAMIC HUD: Single Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Vitals (Avatar, Health, Shield, Jetpack)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(player.hero.primaryColor)
                            .border(2.dp, Color(0xFFFDE68A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.hero.amharicName.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    Column(modifier = Modifier.width(140.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = player.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${player.kills} K / ${player.deaths} D",
                                color = Color(0xFFF59E0B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        val hpRatio = if (player.maxHealth > 0) player.health.toFloat() / player.maxHealth else 0f
                        val hpBrush = if (hpRatio > 0.4f) {
                            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFEF4444)))
                        }
                        SafeProgressBar(
                            ratio = hpRatio,
                            color = if (hpRatio > 0.4f) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = Color(0xFF1E293B),
                            height = 6.dp,
                            brush = hpBrush
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val armRatio = if (player.maxArmor > 0) player.armor.toFloat() / player.maxArmor else 0f
                        val armBrush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
                        SafeProgressBar(
                            ratio = armRatio,
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0xFF0F172A),
                            height = 4.dp,
                            brush = armBrush
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val fuelRatio = if (player.maxJetpackFuel > 0f) player.jetpackFuel / player.maxJetpackFuel else 0f
                        val fuelBrush = if (player.superJetpackTimerMs > 0L) {
                            Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFEA580C), Color(0xFFF59E0B)))
                        }
                        SafeProgressBar(
                            ratio = fuelRatio,
                            color = if (fuelRatio < 0.2f) Color(0xFFEF4444) else Color(0xFFFF6D00),
                            trackColor = Color(0xFF0F172A),
                            height = 4.dp,
                            brush = fuelBrush
                        )
                    }

                    TacticalMiniRadar(engine = engine)
                }

                // Center: Mode Status / Waves / Countdown Timer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val m = engine.matchTimerSec.toInt() / 60
                    val s = engine.matchTimerSec.toInt() % 60
                    val timeStr = "${if (m < 10) "0" else ""}$m:${if (s < 10) "0" else ""}$s"

                    when (engine.mode) {
                        GameMode.HEROIC_CHALLENGE -> {
                            val ch = engine.currentChallenge
                            Text(
                                text = "HEROIC TRIAL: ${ch?.title?.uppercase() ?: "CHALLENGE"}",
                                color = Color(0xFF38BDF8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${ch?.modifierName ?: "Trial"} • Defeat ${engine.campaignTargetKills} Enemies • Kills: ${player.kills} • $timeStr",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        GameMode.CAMPAIGN_MISSION -> {
                            val ch = engine.currentCampaignChapter
                            Text(
                                text = "BATTLE OF ${ch?.title?.uppercase() ?: "ADWA"} (${ch?.dateText ?: "1896"})",
                                color = Color(0xFFF59E0B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Defeat ${engine.campaignTargetKills} Invaders • Kills: ${player.kills} • $timeStr",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        GameMode.SURVIVAL_WAVES -> {
                            Text(
                                text = "WAVE ${engine.currentWave}",
                                color = Color(0xFFEF4444),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${engine.players.count { it.isBot && !it.isDead }} Invaders Left • $timeStr",
                                color = Color(0xFFFDE68A),
                                fontSize = 11.sp
                            )
                        }
                        GameMode.LAN_MULTIPLAYER -> {
                            Text(
                                text = "LAN MATCH • $timeStr",
                                color = Color(0xFF10B981),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${engine.players.size} Warriors Connected",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        GameMode.WEAPON_RANGE -> {
                            Text(
                                text = "WEAPONS RANGE (ልምምድ)",
                                color = Color(0xFF38BDF8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Tap weapons below to test",
                                color = Color(0xFFFDE68A),
                                fontSize = 10.sp
                            )
                        }
                        else -> {
                            Text(
                                text = "${engine.currentArena.title} • $timeStr",
                                color = Color(0xFFF59E0B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "First to 15 Kills",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Right: Active Weapon Card + Taunt & Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.dp, player.currentWeapon.color, RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = player.currentWeapon.amharicName,
                                color = player.currentWeapon.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = if (player.isReloading) "RELOADING..." else "${player.ammoInMag} / ${player.reserveAmmo}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onTauntClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151))
                            .testTag("quick_taunt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Quick Taunt",
                            tint = Color(0xFFFDE68A),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151))
                            .testTag("pause_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Game",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KillFeedOverlay(engine: GameEngine, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.widthIn(max = 280.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End
    ) {
        val recentKills = engine.killEvents.takeLast(4).reversed()
        recentKills.forEach { k ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xB3111827))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = k.killerName,
                    color = Color(0xFFF59E0B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "[${k.weaponTitle}]",
                    color = Color(0xFF38BDF8),
                    fontSize = 9.sp
                )
                Text(
                    text = k.victimName,
                    color = Color(0xFFEF4444),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VirtualJoystick(
    label: String,
    accentColor: Color,
    onValueChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(122.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xEE111827), Color(0xF2070A12))
                    )
                )
                .border(2.dp, Brush.sweepGradient(listOf(accentColor, accentColor.copy(alpha = 0.35f), accentColor)), CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxR = (size.width / 2f) - 14f
                        val diff = down.position - center
                        val dist = hypot(diff.x, diff.y)
                        val clampedDist = min(dist, maxR)
                        val angle = atan2(diff.y, diff.x)
                        val clamped = Offset(cos(angle) * clampedDist, sin(angle) * clampedDist)
                        knobOffset = clamped
                        if (maxR > 0f) {
                            onValueChange(clamped.x / maxR, clamped.y / maxR)
                        }
                        down.consume()

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.find { it.id == pointerId }
                            if (pointer == null || !pointer.pressed) {
                                break
                            }
                            val currDiff = pointer.position - center
                            val currDist = hypot(currDiff.x, currDiff.y)
                            val currClampedDist = min(currDist, maxR)
                            val currAngle = atan2(currDiff.y, currDiff.x)
                            val nextClamped = Offset(cos(currAngle) * currClampedDist, sin(currAngle) * currClampedDist)
                            knobOffset = nextClamped
                            if (maxR > 0f) {
                                onValueChange(nextClamped.x / maxR, nextClamped.y / maxR)
                            }
                            pointer.consume()
                        }
                        knobOffset = Offset.Zero
                        onValueChange(0f, 0f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Tactical guide ring & crosshair ticks
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val r = size.minDimension / 2f
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = accentColor.copy(alpha = 0.22f),
                    radius = r * 0.55f,
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
                drawCircle(
                    color = accentColor.copy(alpha = 0.35f),
                    radius = r,
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }

            // Draggable Knob with tactile radial gradient
            Box(
                modifier = Modifier
                    .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.85f),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(2.5.dp, Color(0xFFFDE68A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFFFDE68A),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RightAimJoystick(
    onAimChange: (Float, Float) -> Unit,
    onFireChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(122.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xEE1E1218), Color(0xF20F080D))
                    )
                )
                .border(2.dp, Brush.sweepGradient(listOf(Color(0xFFEF4444), Color(0xFF7F1D1D), Color(0xFFEF4444))), CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxR = (size.width / 2f) - 14f
                        val diff = down.position - center
                        val dist = hypot(diff.x, diff.y)
                        val clampedDist = min(dist, maxR)
                        val angle = atan2(diff.y, diff.x)
                        val clamped = Offset(cos(angle) * clampedDist, sin(angle) * clampedDist)
                        knobOffset = clamped
                        if (maxR > 0f) {
                            val normX = clamped.x / maxR
                            val normY = clamped.y / maxR
                            onAimChange(normX, normY)
                            if (hypot(normX, normY) > 0.25f) {
                                onFireChange(true)
                            }
                        }
                        down.consume()

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val pointer = event.changes.find { it.id == pointerId }
                            if (pointer == null || !pointer.pressed) {
                                break
                            }
                            val currDiff = pointer.position - center
                            val currDist = hypot(currDiff.x, currDiff.y)
                            val currClampedDist = min(currDist, maxR)
                            val currAngle = atan2(currDiff.y, currDiff.x)
                            val nextClamped = Offset(cos(currAngle) * currClampedDist, sin(currAngle) * currClampedDist)
                            knobOffset = nextClamped
                            if (maxR > 0f) {
                                val currNormX = nextClamped.x / maxR
                                val currNormY = nextClamped.y / maxR
                                onAimChange(currNormX, currNormY)
                                if (hypot(currNormX, currNormY) > 0.25f) {
                                    onFireChange(true)
                                } else {
                                    onFireChange(false)
                                }
                            }
                            pointer.consume()
                        }
                        knobOffset = Offset.Zero
                        onFireChange(false)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Crosshairs & targeting reticle rings
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val r = size.minDimension / 2f
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.25f),
                    radius = r * 0.55f,
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.4f),
                    radius = r,
                    center = c,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
                // 4 axis tick marks
                drawLine(Color(0xFFEF4444).copy(alpha = 0.5f), Offset(c.x, c.y - r), Offset(c.x, c.y - r + 8f), strokeWidth = 2f)
                drawLine(Color(0xFFEF4444).copy(alpha = 0.5f), Offset(c.x, c.y + r), Offset(c.x, c.y + r - 8f), strokeWidth = 2f)
                drawLine(Color(0xFFEF4444).copy(alpha = 0.5f), Offset(c.x - r, c.y), Offset(c.x - r + 8f, c.y), strokeWidth = 2f)
                drawLine(Color(0xFFEF4444).copy(alpha = 0.5f), Offset(c.x + r, c.y), Offset(c.x + r - 8f, c.y), strokeWidth = 2f)
            }

            // Draggable Aim Reticle Knob
            Box(
                modifier = Modifier
                    .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF7F1D1D))
                        )
                    )
                    .border(2.5.dp, Color(0xFFFDE68A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Adjust,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Aim 360°",
            color = Color(0xFFEF4444),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PrimaryFireButton(
    isFireHeld: Boolean,
    onFireChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isFireHeld) 0.90f else 1.0f, label = "fire_scale")

    Box(
        modifier = modifier
            .size(68.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        if (isFireHeld) Color(0xFFFF5252) else Color(0xFFEF4444),
                        Color(0xFF991B1B)
                    )
                )
            )
            .border(2.5.dp, if (isFireHeld) Color(0xFFFDE68A) else Color(0xFFFF8A80), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onFireChange(true)
                    down.consume()
                    val pId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val p = event.changes.find { it.id == pId }
                        if (p == null || !p.pressed) break
                        p.consume()
                    }
                    onFireChange(false)
                }
            }
            .testTag("primary_fire_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Primary Fire",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "FIRE",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "ተኩስ",
                color = Color(0xFFFDE68A),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun JetpackFlyButton(
    isJetpackHeld: Boolean,
    fuelRatio: Float,
    onJetpackChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isJetpackHeld) 0.90f else 1.0f, label = "jetpack_scale")

    Box(
        modifier = modifier
            .size(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        if (isJetpackHeld) Color(0xFFFF9100) else Color(0xFFFF6D00),
                        Color(0xFFB45309)
                    )
                )
            )
            .border(2.dp, if (fuelRatio > 0.2f) Color(0xFFFDE68A) else Color(0xFFEF4444), CircleShape)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onJetpackChange(true)
                    down.consume()
                    val pId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val p = event.changes.find { it.id == pId }
                        if (p == null || !p.pressed) break
                        p.consume()
                    }
                    onJetpackChange(false)
                }
            }
            .testTag("jetpack_fly_button"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FlightTakeoff,
                contentDescription = "Jetpack Fly",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "FLY",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "በረራ",
                color = Color(0xFFFDE68A),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(width = 72.dp, height = 72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.32f), color.copy(alpha = 0.12f), Color(0xFF0F172A))
                )
                else Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
            )
            .border(
                1.5.dp,
                if (enabled) color else Color(0xFF334155),
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) color else Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = badge,
                color = if (enabled) color else Color(0xFF94A3B8),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun TauntSelectorDialog(
    onSelectTaunt: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val taunts = listOf(
        "ጀግና! (Hero!)",
        "ለሀገር! (For the Homeland!)",
        "አድዋ! (Victory of Adwa!)",
        "አልሸነፍም! (I will never surrender!)",
        "እርዱኝ! (Need Backup!)",
        "አሸናፊ! (Victor!)",
        "ጥሩ ጨዋታ! (Good Game!)"
    )

    Card(
        modifier = modifier
            .widthIn(max = 320.dp)
            .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20B0F19)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ethiopian Battle Cries (ፉከራ)",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            taunts.forEach { t ->
                Button(
                    onClick = { onSelectTaunt(t) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = t, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TacticalMiniRadar(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 68.dp, height = 44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xD9090D16))
            .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width <= 1f || size.height <= 1f) return@Canvas
            try {
                val arenaW = if (engine.currentArena.worldWidth <= 0f) 2000f else engine.currentArena.worldWidth
                val arenaH = if (engine.currentArena.worldHeight <= 0f) 1200f else engine.currentArena.worldHeight
                val scaleX = size.width / arenaW
                val scaleY = size.height / arenaH

                // Draw platforms
                for (plat in engine.currentArena.getPlatforms()) {
                    drawRect(
                        color = Color(0x4494A3B8),
                        topLeft = Offset(plat.bounds.left * scaleX, plat.bounds.top * scaleY),
                        size = Size(plat.bounds.width * scaleX, (plat.bounds.height * scaleY).coerceAtLeast(1.5f))
                    )
                }

                // Draw pickups (small gold dots)
                for (p in engine.pickups) {
                    if (p.isAvailable) {
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = 1.2f,
                            center = Offset(p.x * scaleX, p.y * scaleY)
                        )
                    }
                }

                // Draw players
                for (p in engine.players) {
                    if (!p.isDead) {
                        val isSelf = p.id == engine.localPlayerId
                        drawCircle(
                            color = if (isSelf) Color(0xFF10B981) else Color(0xFFEF4444),
                            radius = if (isSelf) 3.5f else 2.2f,
                            center = Offset(p.x * scaleX, p.y * scaleY)
                        )
                    }
                }
            } catch (_: Throwable) {
                // Defensive radar catch
            }
        }
    }
}

@Composable
fun RangeWeaponSelector(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xD90B0F19))
            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(WeaponType.values()) { w ->
            val isSelected = engine.localPlayer.currentWeapon == w
            Button(
                onClick = { engine.equipRangeWeapon(w) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) w.color else Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "T${w.tier} ${w.amharicName}",
                    color = if (isSelected) Color.White else w.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WaveBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xF2D97706), Color(0xF2B45309))
                )
            )
            .border(2.dp, Color(0xFFFDE68A), RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFFDE68A), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

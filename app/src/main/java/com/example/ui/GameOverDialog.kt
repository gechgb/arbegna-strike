package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import com.example.engine.GameEngine

@Composable
fun GameOverDialog(
    engine: GameEngine,
    onRematchClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedPlayers = engine.players.sortedByDescending { it.kills }
    val isLocalWinner = sortedPlayers.firstOrNull()?.id == engine.localPlayerId

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC050811))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .border(
                    1.5.dp,
                    if (isLocalWinner) Brush.sweepGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFDE68A)))
                    else Brush.sweepGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B), Color(0xFFEF4444))),
                    RoundedCornerShape(22.dp)
                )
                .testTag("match_results_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (isLocalWinner) Color(0xFF1E2638) else Color(0xFF261820),
                                Color(0xFF0F1522),
                                Color(0xFF070B13)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trophy Aura
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isLocalWinner) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                1.5.dp,
                                if (isLocalWinner) Color(0xFFFDE68A) else Color(0xFFEF4444),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = if (isLocalWinner) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (engine.mode == com.example.model.GameMode.HEROIC_CHALLENGE) {
                        val ch = engine.currentChallenge
                        Text(
                            text = if (isLocalWinner) "HEROIC TRIUMPH! አሸናፊ!" else "TRIAL FAILED • ይሞክሩ",
                            color = if (isLocalWinner) Color(0xFFF59E0B) else Color(0xFFEF4444),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= engine.campaignStarsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isLocalWinner) "${ch?.title ?: "Challenge"}: Complete! • ${engine.campaignStarsEarned} Stars Earned" else "Defeat ${engine.campaignTargetKills} enemies before time runs out!",
                            color = Color(0xFFFDE68A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (engine.mode == com.example.model.GameMode.CAMPAIGN_MISSION) {
                        val ch = engine.currentCampaignChapter
                        Text(
                            text = if (isLocalWinner) "VICTORY OF ${ch?.title?.uppercase() ?: "ADWA"}!" else "DEFEAT IN ${ch?.title?.uppercase() ?: "BATTLE"}",
                            color = if (isLocalWinner) Color(0xFFF59E0B) else Color(0xFFEF4444),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= engine.campaignStarsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isLocalWinner) "${engine.campaignStarsEarned} Stars Earned • Chapter Completed!" else "Objective Incomplete • Try Again",
                            color = Color(0xFFFDE68A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = if (isLocalWinner) "VICTORY! አሸናፊ!" else "MATCH COMPLETE",
                            color = if (isLocalWinner) Color(0xFFF59E0B) else Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Winner: ${engine.winnerName.ifEmpty { sortedPlayers.firstOrNull()?.name ?: "Unknown" }}",
                            color = Color(0xFFFDE68A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scoreboard Leaderboard
                    Text(
                        text = "FINAL SCOREBOARD • የውጤት ሰሌዳ",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(sortedPlayers) { index, p ->
                            val isLocal = p.id == engine.localPlayerId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLocal) Color(0xFF1B2940) else Color(0xFF111724))
                                    .border(
                                        1.dp,
                                        if (isLocal) Color(0xFFF59E0B) else if (index == 0) Color(0xFFFDE68A).copy(alpha = 0.6f) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Rank Badge
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (index == 0) Color(0xFFD97706) else Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier.size(26.dp).clip(CircleShape).background(p.hero.primaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = p.hero.amharicName.take(1), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = p.name + if (isLocal) " (You)" else "",
                                        color = if (isLocal) Color(0xFFFDE68A) else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(text = "${p.kills} Kills", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text(text = "${p.deaths} Deaths", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMenuClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("return_to_main_menu_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Main Menu", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onRematchClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("play_again_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Play Again (ደግመው)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    engine: GameEngine,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(engine.audio.isMuted) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC050811))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .border(
                    1.5.dp,
                    Brush.sweepGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFDE68A))),
                    RoundedCornerShape(22.dp)
                )
                .testTag("pause_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2638), Color(0xFF0F1522), Color(0xFF070B13))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BATTLE PAUSED • ጨዋታው ቆሟል",
                        color = Color(0xFFF59E0B),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${engine.currentArena.title} • ${engine.mode.title}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sound Toggle Button
                    OutlinedButton(
                        onClick = {
                            isMuted = !isMuted
                            engine.audio.isMuted = isMuted
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isMuted) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isMuted) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Audio Toggle",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMuted) "Audio Sound Effects: MUTED" else "Audio Sound Effects: ACTIVE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Resume Button
                    Button(
                        onClick = onResumeClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("resume_battle_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "RESUME BATTLE (ቀጥል)", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRestartClick,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Restart", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onMenuClick,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Main Menu", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

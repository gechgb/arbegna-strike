package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            .background(Color(0xB3000000))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .border(2.dp, if (isLocalWinner) Color(0xFFF59E0B) else Color(0xFFDC2626), RoundedCornerShape(20.dp))
                .testTag("match_results_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20B0F19)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = if (isLocalWinner) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (engine.mode == com.example.model.GameMode.HEROIC_CHALLENGE) {
                    val ch = engine.currentChallenge
                    Text(
                        text = if (isLocalWinner) "HEROIC TRIUMPH! አሸናፊ!" else "TRIAL FAILED • ይሞክሩ",
                        color = if (isLocalWinner) Color(0xFFF59E0B) else Color(0xFFEF4444),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= engine.campaignStarsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= engine.campaignStarsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
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
                    text = "FINAL SCOREBOARD",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sortedPlayers) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (p.id == engine.localPlayerId) Color(0xFF1E293B) else Color(0xFF111827))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(p.hero.primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = p.hero.amharicName.take(1), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = p.name + if (p.id == engine.localPlayerId) " (You)" else "",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(text = "${p.kills} Kills", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${p.deaths} Deaths", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onMenuClick,
                        modifier = Modifier.weight(1f).testTag("return_to_main_menu_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Main Menu", color = Color.White)
                    }
                    Button(
                        onClick = onRematchClick,
                        modifier = Modifier.weight(1f).testTag("play_again_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Play Again", fontWeight = FontWeight.Black)
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
            .background(Color(0xB3000000))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                .testTag("pause_dialog"),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20B0F19)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BATTLE PAUSED (ጨዋታው ቆሟል)",
                    color = Color(0xFFF59E0B),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${engine.currentArena.title} • ${engine.mode.title}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sound Toggle Button
                OutlinedButton(
                    onClick = {
                        isMuted = !isMuted
                        engine.audio.isMuted = isMuted
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
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
                        text = if (isMuted) "Audio Sound Effects: MUTED" else "Audio Sound Effects: ON",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resume Button
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("resume_battle_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "RESUME BATTLE", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestartClick,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Restart", color = Color.White, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onMenuClick,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Main Menu", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

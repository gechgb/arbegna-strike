package com.example.ui

import androidx.activity.compose.BackHandler
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
import com.example.model.ChallengeRepository
import com.example.model.HeroicChallenge

/**
 * Traditional Ethiopian Tibeb Woven Ribbon Decoration
 * Geometric diamond & stepped pattern using gold, crimson red, and emerald green.
 */
@Composable
fun TibebPatternRibbon(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF0F172A))
    ) {
        val colors = listOf(
            Color(0xFFDC2626), Color(0xFFF59E0B), Color(0xFF10B981),
            Color(0xFFF59E0B), Color(0xFFDC2626), Color(0xFF10B981)
        )
        for (i in 0 until 18) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colors[i % colors.size])
            )
        }
    }
}

@Composable
fun HeroicChallengesScreen(
    onBack: () -> Unit,
    onStartChallenge: (HeroicChallenge) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val challenges = remember { ChallengeRepository.challenges }
    val totalStars = remember { ChallengeRepository.getTotalStars() }
    var selectedChallenge by remember {
        mutableStateOf(challenges.firstOrNull { it.isUnlocked } ?: challenges[0])
    }

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
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x40F59E0B), CircleShape)
                            .testTag("challenges_back_button")
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
                                text = "HEROIC CHALLENGES",
                                color = Color(0xFFF59E0B),
                                fontSize = if (isCompact) 18.sp else 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Ethiopian Flag Ribbon
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
                            text = "የጀግንነት ፈተናዎች - Special Weapon & Tactical Combat Trials",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Total Stars Badge with Jano gold trim
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$totalStars / 12",
                            color = Color(0xFFFDE68A),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TibebPatternRibbon()
            Spacer(modifier = Modifier.height(10.dp))

            // MAIN CONTENT (Adaptive 2-column on wide screens, stacked on phones)
            if (isCompact) {
                // Portrait Layout
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "SELECT CHALLENGE TRIAL",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(challenges) { ch ->
                        ChallengeListCard(
                            challenge = ch,
                            isSelected = ch.id == selectedChallenge.id,
                            onClick = { selectedChallenge = ch }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        ChallengeDetailCard(
                            challenge = selectedChallenge,
                            onStart = { onStartChallenge(selectedChallenge) }
                        )
                    }
                }
            } else {
                // Landscape / Tablet Split Layout
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Challenge List
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = "CHALLENGE TRIALS",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(challenges) { ch ->
                                ChallengeListCard(
                                    challenge = ch,
                                    isSelected = ch.id == selectedChallenge.id,
                                    onClick = { selectedChallenge = ch }
                                )
                            }
                        }
                    }

                    // Right Column: Challenge Detail & Deploy Button
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                    ) {
                        ChallengeDetailCard(
                            challenge = selectedChallenge,
                            onStart = { onStartChallenge(selectedChallenge) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeListCard(
    challenge: HeroicChallenge,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color(0xFFF59E0B)
        challenge.isUnlocked -> Color(0x33F59E0B)
        else -> Color(0x22FFFFFF)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = challenge.isUnlocked) { onClick() }
            .testTag("challenge_item_${challenge.id}"),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Color(0xFF1E293B)
                challenge.isUnlocked -> Color(0xFF111827)
                else -> Color(0x400B0F19)
            }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Challenge Number or Lock Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (challenge.isUnlocked) Color(0xFFD97706) else Color(0xFF334155)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (challenge.isUnlocked) {
                        Text(
                            text = "#${challenge.id}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = challenge.title,
                            color = if (challenge.isUnlocked) Color.White else Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = challenge.amharicTitle,
                        color = if (challenge.isUnlocked) Color(0xFFF59E0B) else Color(0xFF475569),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Modifier: ${challenge.modifierName}",
                        color = if (challenge.isUnlocked) Color(0xFF38BDF8) else Color(0xFF475569),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stars
            Column(horizontalAlignment = Alignment.End) {
                if (challenge.isUnlocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= challenge.starsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (challenge.highScore > 0) {
                        Text(
                            text = "${challenge.highScore} PTS",
                            color = Color(0xFFFDE68A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "LOCKED",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeDetailCard(
    challenge: HeroicChallenge,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFFD97706), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header with badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = challenge.title.uppercase(),
                        color = Color(0xFFF59E0B),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = challenge.amharicTitle,
                        color = Color(0xFFFDE68A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDC2626))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = challenge.badge.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            TibebPatternRibbon()
            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = challenge.description,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Combat Modifier Callout
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Modifier",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "MODIFIER: ${challenge.modifierName}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = challenge.modifierDescription,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tactical Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBadge(
                    title = "TARGET",
                    value = "${challenge.targetKills} Kills",
                    color = Color(0xFFEF4444),
                    icon = Icons.Default.MilitaryTech,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    title = "TIME LIMIT",
                    value = "${challenge.timeLimitSec.toInt()}s",
                    color = Color(0xFFF59E0B),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                StatBadge(
                    title = "LOADOUT",
                    value = challenge.lockedWeapon.title.take(9),
                    color = Color(0xFF10B981),
                    icon = Icons.Default.Security,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arena & Costume Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(challenge.playerHero.primaryColor)
                                .border(1.dp, Color(0xFFFDE68A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = challenge.playerHero.amharicName.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Warrior: ${challenge.playerHero.title} (${challenge.playerHero.amharicName})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Costume: Kuta & Kumeta with Jano Sash",
                                color = Color(0xFFF59E0B),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = challenge.arena.title,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = challenge.arena.amharicName,
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = onStart,
                enabled = challenge.isUnlocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("deploy_challenge_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (challenge.isUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (challenge.isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (challenge.isUnlocked) "DEPLOY TO CHALLENGE (ፈተናውን ጀምር)" else "LOCKED (ቀዳሚውን ፈተና ይጨርሱ)",
                    color = if (challenge.isUnlocked) Color(0xFF0F172A) else Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

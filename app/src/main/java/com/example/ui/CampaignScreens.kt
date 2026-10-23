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
import com.example.model.CampaignChapter
import com.example.model.CampaignRepository

@Composable
fun CampaignSelectScreen(
    onBack: () -> Unit,
    onStartChapter: (CampaignChapter) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val chapters = remember { CampaignRepository.chapters }
    val totalStars = remember { CampaignRepository.getTotalStarsEarned() }
    var selectedChapter by remember { mutableStateOf(chapters.firstOrNull { it.isUnlocked } ?: chapters[0]) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF090D16))
                )
            )
    ) {
        val isCompact = maxWidth < 720.dp

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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("campaign_back_button")
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
                                text = "HISTORICAL CAMPAIGN",
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
                            text = "የኢትዮጵያ ታሪካዊ ዘመቻዎች • Legendary Battles",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Total Stars Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                            text = "$totalStars / 12 Stars",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isCompact) {
                // PORTRAIT LAYOUT: Scrollable list of mission cards with embedded details
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chapters) { ch ->
                        CampaignMissionCard(
                            chapter = ch,
                            isSelected = ch.id == selectedChapter.id,
                            onSelect = { selectedChapter = ch },
                            onStart = { onStartChapter(ch) }
                        )
                    }
                }
            } else {
                // LANDSCAPE / WIDE LAYOUT: Two Panes (Chapter List on Left, Mission Briefing on Right)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: Mission selector list (42% width)
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chapters) { ch ->
                            CompactChapterItem(
                                chapter = ch,
                                isSelected = ch.id == selectedChapter.id,
                                onClick = { selectedChapter = ch }
                            )
                        }
                    }

                    // Right Pane: Detailed Mission Dossier & Launch (58% width)
                    Card(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight()
                            .border(1.5.dp, Color(0x33F59E0B), RoundedCornerShape(18.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xD90B0F19)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        CampaignDossier(
                            chapter = selectedChapter,
                            onStart = { onStartChapter(selectedChapter) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignMissionCard(
    chapter: CampaignChapter,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = chapter.isUnlocked) { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (!chapter.isUnlocked) Color(0xFF334155) else if (isSelected) Color(0xFFF59E0B) else Color(0x44F59E0B),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("campaign_chapter_${chapter.id}_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (!chapter.isUnlocked) Color(0xFF0F172A).copy(alpha = 0.6f) else Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Chapter Number, Title, Stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (chapter.isUnlocked) Color(0xFFD97706) else Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (chapter.isUnlocked) {
                            Text(
                                text = "${chapter.id}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = chapter.title,
                            color = if (chapter.isUnlocked) Color.White else Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${chapter.dateText} • ${chapter.amharicTitle}",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Stars display
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= chapter.starsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Commander & Arena badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Leader: ${chapter.leaderName}", fontSize = 10.sp, color = Color(0xFFFDE68A)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF0F172A))
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text(chapter.arena.title, fontSize = 10.sp, color = Color(0xFF38BDF8)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF0F172A))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chapter.description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Objectives summary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = chapter.objectiveDescription,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (chapter.isUnlocked) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("launch_chapter_${chapter.id}_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (chapter.starsEarned > 0) "REPLAY BATTLE" else "DEPLOY TO BATTLE",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOCKED • Complete Chapter ${chapter.id - 1} First",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactChapterItem(
    chapter: CampaignChapter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = chapter.isUnlocked) { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (!chapter.isUnlocked) Color(0xFF334155) else if (isSelected) Color(0xFFF59E0B) else Color(0x33F59E0B),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (chapter.isUnlocked) Color(0xFFD97706) else Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    if (chapter.isUnlocked) {
                        Text(text = "${chapter.id}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = chapter.title,
                        color = if (chapter.isUnlocked) Color.White else Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chapter.dateText,
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= chapter.starsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CampaignDossier(
    chapter: CampaignChapter,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Title & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CHAPTER ${chapter.id} • ${chapter.dateText}",
                        color = Color(0xFFF59E0B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = chapter.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = chapter.amharicTitle,
                        color = Color(0xFFFDE68A),
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= chapter.starsEarned) Color(0xFFF59E0B) else Color(0xFF334155),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0x33F59E0B))

            // Historical Commander & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "COMMANDER", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = chapter.leaderName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "BATTLEFIELD", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(text = chapter.arena.title, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Briefing Synopsis
            Text(
                text = "TACTICAL BRIEFING",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = chapter.description,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Star Objectives
            Text(
                text = "VICTORY OBJECTIVE",
                color = Color(0xFFF59E0B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chapter.objectiveDescription,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Action Button
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("launch_chapter_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (chapter.starsEarned > 0) "REPLAY BATTLE" else "ENTER THE BATTLEFIELD",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}

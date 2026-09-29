package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdventureLevel
import com.example.model.AdventureLevelsCatalog
import com.example.model.GameMode
import com.example.model.LevelDifficulty
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun AdventureMapScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allLevelProgress by viewModel.allLevelProgress.collectAsState()

    var selectedFilter by remember { mutableStateOf<LevelDifficulty?>(null) }

    val filteredLevels = remember(selectedFilter) {
        if (selectedFilter == null) {
            AdventureLevelsCatalog.levels
        } else {
            AdventureLevelsCatalog.getLevelsByDifficulty(selectedFilter!!)
        }
    }

    val totalStarsCollected = remember(allLevelProgress) {
        allLevelProgress.values.sumOf { it.stars }
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(activeTheme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x331E293B))
                            .testTag("adventure_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "LEVEL SECTORS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                // Stars total counter
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x331E293B))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Text(
                        text = "$totalStarsCollected / 75",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Difficulty Filter Chips (Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyFilterChip(
                    label = "ALL (25)",
                    isSelected = selectedFilter == null,
                    color = activeTheme.accentColor,
                    onClick = { selectedFilter = null }
                )
                LevelDifficulty.entries.forEach { diff ->
                    DifficultyFilterChip(
                        label = "${diff.label} (${when (diff) {
                            LevelDifficulty.EASY -> "1–5"
                            LevelDifficulty.MEDIUM -> "6–10"
                            LevelDifficulty.HARD -> "11–15"
                            LevelDifficulty.EXPERT -> "16–20"
                            LevelDifficulty.INSANE -> "21–25"
                        }})",
                        isSelected = selectedFilter == diff,
                        color = Color(diff.colorHex),
                        onClick = { selectedFilter = diff }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sector levels list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLevels) { level ->
                    val isUnlocked = level.levelNumber <= userProfile.currentLevel
                    val starsEarned = allLevelProgress[level.levelNumber]?.stars ?: 0

                    AdventureLevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        starsEarned = starsEarned,
                        accentColor = activeTheme.accentColor,
                        onPlay = {
                            viewModel.startNewGame(GameMode.ADVENTURE, level.levelNumber)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DifficultyFilterChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0x221E293B))
            .border(
                width = 1.dp,
                color = if (isSelected) color else Color(0x22FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) color else Color(0xCCFFFFFF),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}

@Composable
fun AdventureLevelCard(
    level: AdventureLevel,
    isUnlocked: Boolean,
    starsEarned: Int,
    accentColor: Color,
    onPlay: () -> Unit
) {
    val diffColor = Color(level.difficulty.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = isUnlocked, onClick = onPlay)
            .testTag("adventure_level_${level.levelNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0x331E293B) else Color(0x180F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isUnlocked) 1.2.dp else 1.dp,
            color = if (isUnlocked) diffColor.copy(alpha = 0.5f) else Color(0x15FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Level Number Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isUnlocked) diffColor.copy(alpha = 0.2f) else Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(
                            text = "${level.levelNumber}",
                            color = diffColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0x66FFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    // Title and Difficulty Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(diffColor)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = level.difficulty.label,
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = level.title,
                            color = if (isUnlocked) Color.White else Color(0x66FFFFFF),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = level.description,
                        color = Color(0x88FFFFFF),
                        fontSize = 11.sp,
                        maxLines = 1
                    )

                    // Target Badges & Stars
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stars
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(3) { starIndex ->
                                val filled = starIndex < starsEarned
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (filled) Color(0xFFFFD700) else Color(0x33FFFFFF),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Text(
                            text = "• ${level.targetLines} lines",
                            color = Color(0xAAFFFFFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (level.hasIceBlocks) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(11.dp))
                                Text("Ice", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (level.hasObstacles) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.GridOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                                Text("Obstacles", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isUnlocked) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = diffColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                }
            }
        }
    }
}

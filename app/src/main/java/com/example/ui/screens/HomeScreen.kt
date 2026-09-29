package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.model.GameMode
import com.example.theme.ThemeConfig
import com.example.viewmodel.AppScreen

@Composable
fun HomeScreen(
    userProfile: UserProfileEntity,
    theme: ThemeConfig,
    onStartGame: (GameMode) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with Currencies and Settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Daily Streak Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x331E293B))
                            .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = "Streak", tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                        Text(
                            text = "DAY ${userProfile.dailyStreak} STREAK",
                            color = Color(0xFFFF9800),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Currencies
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CurrencyChip(icon = Icons.Default.Star, count = userProfile.coins, color = Color(0xFFFFD700))
                        CurrencyChip(icon = Icons.Default.Diamond, count = userProfile.gems, color = Color(0xFF00E5FF))
                        IconButton(
                            onClick = { onNavigate(AppScreen.SETTINGS) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x331E293B))
                                .testTag("home_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                }
            }

            // Hero Brand Title Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF0F172A)
                                )
                            )
                        )
                        .border(1.5.dp, theme.accentColor.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚡ BLOCKFLOW ⚡",
                            color = theme.accentColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "NEXT-GEN 2026 CASUAL PUZZLE",
                            color = Color(0xAAFFFFFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // High Score Badge
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x44000000))
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Text(
                                text = "PERSONAL BEST: ${userProfile.highScore}",
                                color = Color(0xFFFFD700),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Primary Big "PLAY CLASSIC" Button
            item {
                Button(
                    onClick = { onStartGame(GameMode.CLASSIC) },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), spotColor = theme.accentColor)
                        .testTag("play_classic_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                        Text(
                            text = "PLAY CLASSIC",
                            color = Color.Black,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Secondary Game Modes Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeCard(
                        title = "ADVENTURE",
                        desc = "Levels 1–8",
                        icon = Icons.Default.Explore,
                        color = Color(0xFFA855F7),
                        onClick = { onNavigate(AppScreen.ADVENTURE_MAP) },
                        modifier = Modifier.weight(1f),
                        testTag = "mode_adventure_btn"
                    )

                    ModeCard(
                        title = "DAILY QUEST",
                        desc = "+500 Coins",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFFF007A),
                        onClick = { onNavigate(AppScreen.DAILY_CHALLENGE) },
                        modifier = Modifier.weight(1f),
                        testTag = "mode_daily_btn"
                    )

                    ModeCard(
                        title = "TIME RUSH",
                        desc = "90s Blitz",
                        icon = Icons.Default.Timer,
                        color = Color(0xFFFBBF24),
                        onClick = { onStartGame(GameMode.TIME_RUSH) },
                        modifier = Modifier.weight(1f),
                        testTag = "mode_timerush_btn"
                    )
                }
            }

            // Bottom Navigation Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BottomNavAction(
                        title = "THEMES",
                        icon = Icons.Default.Palette,
                        theme = theme,
                        onClick = { onNavigate(AppScreen.SHOP) },
                        modifier = Modifier.weight(1f),
                        testTag = "home_shop_btn"
                    )

                    BottomNavAction(
                        title = "BADGES",
                        icon = Icons.Default.AutoAwesome,
                        theme = theme,
                        onClick = { onNavigate(AppScreen.ACHIEVEMENTS) },
                        modifier = Modifier.weight(1f),
                        testTag = "home_badges_btn"
                    )

                    BottomNavAction(
                        title = "RANKS",
                        icon = Icons.Default.Leaderboard,
                        theme = theme,
                        onClick = { onNavigate(AppScreen.LEADERBOARD) },
                        modifier = Modifier.weight(1f),
                        testTag = "home_ranks_btn"
                    )
                }
            }

            // Stats Quick Glance
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x331E293B))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatGlance("GAMES", "${userProfile.totalGamesPlayed}")
                    StatGlance("LINES", "${userProfile.totalLinesCleared}")
                    StatGlance("BEST COMBO", "${userProfile.bestCombo}x")
                    StatGlance("LEVEL", "${userProfile.currentLevel}")
                }
            }
        }
    }
}

@Composable
fun CurrencyChip(icon: ImageVector, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x331E293B))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(text = "$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ModeCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .height(95.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(desc, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun BottomNavAction(
    title: String,
    icon: ImageVector,
    theme: ThemeConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x221E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cellEmptyBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = theme.accentColor, modifier = Modifier.size(22.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun StatGlance(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0x77FFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

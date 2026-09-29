package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun DailyChallengeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyChallenge by viewModel.dailyChallenge.collectAsState()

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x331E293B))
                        .testTag("daily_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "DAILY QUEST & STREAK",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Mission Card
                item {
                    val desc = dailyChallenge?.description ?: "Reach a 5x Combo streak"
                    val target = dailyChallenge?.targetValue ?: 5
                    val current = minOf(userProfile.bestCombo, target)
                    val isDone = current >= target

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, activeTheme.accentColor)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TODAY'S CHALLENGE", color = activeTheme.accentColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                Text("REWARD: +500 🪙 +50 💎", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(desc, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = activeTheme.accentColor,
                                trackColor = Color(0x33FFFFFF)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Progress", color = Color(0x88FFFFFF), fontSize = 12.sp)
                                Text("$current / $target", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.startNewGame(GameMode.DAILY) },
                                colors = ButtonDefaults.buttonColors(containerColor = activeTheme.accentColor),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().testTag("start_daily_quest_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                Text("PLAY QUEST NOW", color = Color.Black, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }

                // 7-Day Login Streak Reward Calendar
                item {
                    Text(
                        text = "7-DAY STREAK CALENDAR",
                        color = activeTheme.accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (day in 1..7) {
                            val isClaimed = day <= userProfile.dailyStreak
                            val isToday = day == userProfile.dailyStreak

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isToday) Color(0x5500F0FF) else if (isClaimed) Color(0x3310B981) else Color(0x221E293B)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isToday) activeTheme.accentColor else Color(0x22FFFFFF)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("D$day", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = if (isClaimed) Icons.Default.Check else if (day == 7) Icons.Default.Diamond else Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isClaimed) Color(0xFF10B981) else if (day == 7) Color(0xFF00E5FF) else Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+${day * 50}",
                                        color = Color(0xCCFFFFFF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
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

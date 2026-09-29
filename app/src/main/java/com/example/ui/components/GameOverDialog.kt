package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.theme.ThemeConfig

@Composable
fun GameOverDialog(
    isOpen: Boolean,
    score: Int,
    bestScore: Int,
    linesCleared: Int,
    combo: Int,
    isNewRecord: Boolean,
    coinsEarned: Int,
    theme: ThemeConfig,
    onWatchAdToRevive: () -> Unit,
    onWatchAdToDoubleCoins: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit,
    onShare: () -> Unit
) {
    if (!isOpen) return

    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "animated_score"
    )

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(32.dp))
                .background(theme.boardBackground)
                .border(2.dp, if (isNewRecord) Color(0xFFFFD700) else theme.boardBorder, RoundedCornerShape(32.dp))
                .padding(22.dp)
                .testTag("game_over_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isNewRecord) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFD700))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "★ NEW RECORD! ★",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = "GAME OVER",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                // Animated Final Score
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FINAL SCORE",
                        color = Color(0xAAFFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$animatedScore",
                        color = theme.accentColor,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x331E293B))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatSummaryItem(label = "BEST", value = "$bestScore", color = Color(0xFFFFD700))
                    StatSummaryItem(label = "LINES", value = "$linesCleared", color = Color.White)
                    StatSummaryItem(label = "MAX COMBO", value = "${combo}x", color = theme.secondaryAccent)
                    StatSummaryItem(label = "+COINS", value = "+$coinsEarned", color = Color(0xFFFBBF24))
                }

                // ==========================================
                // REWARDED AD OPT-IN (Second Chance & 2X)
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x551E1B4B), Color(0x3300E5FF))
                            )
                        )
                        .border(1.2.dp, Color(0xFF00E5FF), RoundedCornerShape(18.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Text(
                            text = "SECOND CHANCE REVIVE",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Button(
                        onClick = onWatchAdToRevive,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0xFF00E5FF))
                            .testTag("revive_ad_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            Text(
                                text = "REVIVE & KEEP PLAYING",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }

                    // Optional 2x Coins button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable(onClick = onWatchAdToDoubleCoins)
                            .padding(vertical = 6.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Text(
                            text = "Or watch ad to double coins: +${coinsEarned * 2} 🪙",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Standard Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReplay,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("game_over_replay_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                        Text("REPLAY", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }

                    Button(
                        onClick = onShare,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("game_over_share_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Text("SHARE", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                    Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
fun StatSummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0x88FFFFFF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AdventureLevel
import com.example.theme.ThemeConfig
import kotlinx.coroutines.delay

@Composable
fun LevelVictoryDialog(
    isOpen: Boolean,
    level: AdventureLevel,
    starsEarned: Int,
    score: Int,
    coinsEarned: Int,
    gemsEarned: Int,
    hasNextLevel: Boolean,
    theme: ThemeConfig,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMap: () -> Unit
) {
    if (!isOpen) return

    val starScale1 = remember { Animatable(0f) }
    val starScale2 = remember { Animatable(0f) }
    val starScale3 = remember { Animatable(0f) }

    LaunchedEffect(isOpen) {
        delay(200)
        if (starsEarned >= 1) starScale1.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        if (starsEarned >= 2) {
            delay(150)
            starScale2.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        }
        if (starsEarned >= 3) {
            delay(150)
            starScale3.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
        }
    }

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(32.dp))
                .background(theme.boardBackground)
                .border(2.dp, theme.accentColor, RoundedCornerShape(32.dp))
                .padding(24.dp)
                .testTag("level_victory_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Difficulty Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(level.difficulty.colorHex).copy(alpha = 0.25f))
                        .border(1.dp, Color(level.difficulty.colorHex), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${level.difficulty.label} • LEVEL ${level.levelNumber}",
                        color = Color(level.difficulty.colorHex),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "SECTOR CLEARED!",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = level.title,
                    color = Color(0xCCFFFFFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // 3 Stars Row with animated pops
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star 1",
                        tint = if (starsEarned >= 1) Color(0xFFFFD700) else Color(0x33FFFFFF),
                        modifier = Modifier
                            .size(46.dp)
                            .scale(if (starsEarned >= 1) starScale1.value else 0.8f)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star 2",
                        tint = if (starsEarned >= 2) Color(0xFFFFD700) else Color(0x33FFFFFF),
                        modifier = Modifier
                            .size(56.dp)
                            .scale(if (starsEarned >= 2) starScale2.value else 0.8f)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star 3",
                        tint = if (starsEarned >= 3) Color(0xFFFFD700) else Color(0x33FFFFFF),
                        modifier = Modifier
                            .size(46.dp)
                            .scale(if (starsEarned >= 3) starScale3.value else 0.8f)
                    )
                }

                // Stats breakdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x331E293B))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatSummaryItem(label = "SCORE", value = "$score", color = theme.accentColor)
                    StatSummaryItem(label = "+COINS", value = "+$coinsEarned 🪙", color = Color(0xFFFFD700))
                    if (gemsEarned > 0) {
                        StatSummaryItem(label = "+GEMS", value = "+$gemsEarned 💎", color = Color(0xFF00E5FF))
                    }
                }

                // Buttons
                if (hasNextLevel) {
                    Button(
                        onClick = onNextLevel,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("victory_next_level_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("NEXT LEVEL", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReplay,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Text("REPLAY", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }

                    Button(
                        onClick = onMap,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = Color.White)
                        Text("MAP", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

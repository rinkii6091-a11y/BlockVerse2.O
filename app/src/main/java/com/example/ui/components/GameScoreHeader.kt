package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.ScoreHeaderState
import com.example.theme.ThemeConfig
import kotlinx.coroutines.delay

/**
 * UI Header Component that tracks and displays the user's current game score,
 * best score, active combo multiplier, and current game mode.
 *
 * Driven by ViewModel-backed [ScoreHeaderState].
 */
@Composable
fun GameScoreHeader(
    state: ScoreHeaderState,
    theme: ThemeConfig,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth score roll-up animation
    val animatedScore by animateIntAsState(
        targetValue = state.currentScore,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "header_score_anim"
    )

    // Pulse animation when score increments
    var isPulsing by remember { mutableStateOf(false) }
    val scoreScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "score_pulse"
    )

    LaunchedEffect(state.currentScore) {
        if (state.currentScore > 0) {
            isPulsing = true
            delay(180)
            isPulsing = false
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("game_score_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode & Level Pill Badge
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val (modeTitle, modeIcon, badgeColor) = when (state.gameMode) {
                GameMode.CLASSIC -> Triple("CLASSIC", Icons.Default.EmojiEvents, theme.accentColor)
                GameMode.ADVENTURE -> Triple("LEVEL ${state.adventureLevel}", Icons.Default.Explore, Color(0xFFA855F7))
                GameMode.DAILY -> Triple("DAILY", Icons.Default.Schedule, Color(0xFFFF007A))
                GameMode.TIME_RUSH -> Triple("TIME RUSH", Icons.Default.Timer, Color(0xFFFBBF24))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x331E293B))
                    .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = modeIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = modeTitle,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Active Combo indicator if in combo streak
            AnimatedVisibility(
                visible = state.combo > 1,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xE6FF007A))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${state.combo}X COMBO",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Center: Animated Current Score & High Score Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$animatedScore",
                color = if (state.isNewRecord) Color(0xFFFFD700) else Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .scale(scoreScale)
                    .testTag("current_score_display")
            )

            // High Score or New Record indicator
            if (state.isNewRecord) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFF59E0B))
                            )
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .testTag("new_record_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "NEW BEST!",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "BEST ${state.bestScore}",
                    color = Color(0x99FFFFFF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.testTag("best_score_display")
                )
            }
        }

        // Right: Pause Action Button
        IconButton(
            onClick = onPauseClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0x331E293B))
                .border(1.dp, Color(0x22FFFFFF), CircleShape)
                .testTag("game_pause_btn")
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

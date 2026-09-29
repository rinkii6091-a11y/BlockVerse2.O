package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdventureLevel
import com.example.theme.ThemeConfig

@Composable
fun LevelObjectivesHud(
    level: AdventureLevel,
    currentScore: Int,
    currentLines: Int,
    currentMaxCombo: Int,
    theme: ThemeConfig,
    modifier: Modifier = Modifier
) {
    val linesProgress = (currentLines.toFloat() / level.targetLines.toFloat()).coerceIn(0f, 1f)
    val scoreProgress = (currentScore.toFloat() / level.targetScore.toFloat()).coerceIn(0f, 1f)
    val diffColor = Color(level.difficulty.colorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x331E293B))
            .border(1.dp, diffColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Difficulty & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(diffColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = level.difficulty.label,
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "L${level.levelNumber}: ${level.title}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Target Objectives
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lines tracker
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Lines: ${minOf(currentLines, level.targetLines)}/${level.targetLines}",
                        color = if (currentLines >= level.targetLines) Color(0xFF10B981) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { linesProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (currentLines >= level.targetLines) Color(0xFF10B981) else theme.accentColor,
                        trackColor = Color(0x33FFFFFF)
                    )
                }

                // Score tracker
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Goal: ${minOf(currentScore, level.targetScore)}/${level.targetScore}",
                        color = if (currentScore >= level.targetScore) Color(0xFFFFD700) else Color(0xCCFFFFFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { scoreProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }
        }
    }
}

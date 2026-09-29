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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AchievementEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

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
                            .testTag("achievements_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "ACHIEVEMENTS",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                // Title Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x331E293B))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "FLOW MASTER",
                        color = activeTheme.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { ach ->
                    val (current, isUnlocked) = when (ach.id) {
                        "first_clear" -> userProfile.totalLinesCleared to (userProfile.totalLinesCleared >= 1)
                        "combo_3" -> userProfile.bestCombo to (userProfile.bestCombo >= 3)
                        "combo_6" -> userProfile.bestCombo to (userProfile.bestCombo >= 6)
                        "score_5k" -> userProfile.highScore to (userProfile.highScore >= 5000)
                        "score_15k" -> userProfile.highScore to (userProfile.highScore >= 15000)
                        "lines_50" -> userProfile.totalLinesCleared to (userProfile.totalLinesCleared >= 50)
                        "lines_200" -> userProfile.totalLinesCleared to (userProfile.totalLinesCleared >= 200)
                        else -> 0 to false
                    }

                    AchievementCard(
                        title = ach.title,
                        desc = ach.description,
                        current = current,
                        target = ach.target,
                        isUnlocked = isUnlocked,
                        rewardCoins = ach.rewardCoins,
                        rewardGems = ach.rewardGems,
                        accentColor = activeTheme.accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    title: String,
    desc: String,
    current: Int,
    target: Int,
    isUnlocked: Boolean,
    rewardCoins: Int,
    rewardGems: Int,
    accentColor: Color
) {
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0x331E293B) else Color(0x220F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) accentColor.copy(alpha = 0.6f) else Color(0x22FFFFFF)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) accentColor.copy(alpha = 0.2f) else Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isUnlocked) accentColor else Color(0x66FFFFFF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text(desc, color = Color(0x99FFFFFF), fontSize = 11.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (rewardCoins > 0) {
                        Text("+$rewardCoins 🪙", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (rewardGems > 0) {
                        Text("+$rewardGems 💎", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = Color(0x33FFFFFF)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${minOf(current, target)} / $target",
                    color = Color(0x77FFFFFF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

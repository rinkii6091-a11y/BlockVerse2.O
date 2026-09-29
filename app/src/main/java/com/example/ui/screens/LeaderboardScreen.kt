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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val title: String,
    val score: Int,
    val combo: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val dailyEntries = listOf(
        LeaderboardEntry(1, "NeonSpecter", "BLOCK GOD", 34820, 14),
        LeaderboardEntry(2, "FlowCipher", "COMBO BEAST", 29400, 11),
        LeaderboardEntry(3, "CyberValkyrie", "GRID MASTER", 26150, 9),
        LeaderboardEntry(4, "YOU", "FLOW SEEKER", userProfile.highScore, userProfile.bestCombo, true),
        LeaderboardEntry(5, "GridPhantom", "OVERDRIVE", 21300, 8),
        LeaderboardEntry(6, "KronoPulse", "PUZZLE KING", 18950, 7),
        LeaderboardEntry(7, "GlitchFox", "NEON RUNNER", 16400, 6),
        LeaderboardEntry(8, "VortexPrime", "CHALLENGER", 14200, 5)
    ).sortedByDescending { it.score }

    val allTimeEntries = listOf(
        LeaderboardEntry(1, "AetherZenith", "MYTHIC GOD", 68240, 22),
        LeaderboardEntry(2, "QuantumSlayer", "ULTRA FLOW", 59810, 18),
        LeaderboardEntry(3, "NeonSpecter", "BLOCK GOD", 48600, 15),
        LeaderboardEntry(4, "ZeroLag", "COMBO TITAN", 42100, 13),
        LeaderboardEntry(5, "CyberValkyrie", "GRID MASTER", 39400, 12),
        LeaderboardEntry(6, "YOU", "FLOW SEEKER", userProfile.highScore, userProfile.bestCombo, true),
        LeaderboardEntry(7, "KronoPulse", "PUZZLE KING", 31050, 10),
        LeaderboardEntry(8, "PixelNova", "HYPE BEAST", 27400, 9)
    ).sortedByDescending { it.score }

    val displayedList = if (selectedTab == 0) dailyEntries else allTimeEntries

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
                        .testTag("leaderboard_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "HALL OF FAME",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0x331E293B),
                contentColor = activeTheme.accentColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = activeTheme.accentColor
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("TODAY'S RUSH", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ALL-TIME LEGENDS", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(displayedList) { index, entry ->
                    LeaderboardCard(
                        entry = entry.copy(rank = index + 1),
                        accentColor = activeTheme.accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry, accentColor: Color) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) Color(0x551E3A8A) else Color(0x331E293B)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (entry.isCurrentUser) 1.5.dp else 1.dp,
            color = if (entry.isCurrentUser) accentColor else Color(0x1AFFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank number
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (entry.rank <= 3) rankColor.copy(alpha = 0.2f) else Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        color = rankColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = entry.name,
                            color = if (entry.isCurrentUser) accentColor else Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (entry.isCurrentUser) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text("YOU", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Text(
                        text = entry.title,
                        color = Color(0x88FFFFFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score}",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${entry.combo}x Combo",
                    color = Color(0xFFFF007A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

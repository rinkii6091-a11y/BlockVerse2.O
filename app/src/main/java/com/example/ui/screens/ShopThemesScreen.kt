package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PowerUpType
import com.example.theme.GameThemes
import com.example.theme.ThemeConfig
import com.example.theme.ThemeId
import com.example.ui.components.SingleBlockCell
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun ShopThemesScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val unlockedSet = userProfile.unlockedThemes.split(",").toSet()

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
                            .testTag("shop_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        text = "THEME STUDIO",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                // Balance
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyChip(icon = Icons.Default.Star, count = userProfile.coins, color = Color(0xFFFFD700))
                    CurrencyChip(icon = Icons.Default.Diamond, count = userProfile.gems, color = Color(0xFF00E5FF))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "VISUAL THEMES",
                        color = activeTheme.accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                items(GameThemes.allThemes) { theme ->
                    ThemeCard(
                        theme = theme,
                        isEquipped = activeTheme.id == theme.id,
                        isUnlocked = unlockedSet.contains(theme.id.name),
                        onSelect = { viewModel.selectTheme(theme.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "POWER-UP SUPPLIES",
                        color = activeTheme.accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                item {
                    PowerUpShopRow(
                        title = "Smash Hammer +1",
                        desc = "Destroy any single block on grid",
                        icon = Icons.Default.Build,
                        cost = 150,
                        onBuy = { viewModel.buyPowerUp(PowerUpType.HAMMER, 150) }
                    )
                }

                item {
                    PowerUpShopRow(
                        title = "Shuffle Reroll +1",
                        desc = "Instantly deal fresh 3 block pieces",
                        icon = Icons.Default.Refresh,
                        cost = 120,
                        onBuy = { viewModel.buyPowerUp(PowerUpType.SHUFFLE, 120) }
                    )
                }

                item {
                    PowerUpShopRow(
                        title = "Undo Charge +1",
                        desc = "Revert your last placed piece",
                        icon = Icons.Default.Undo,
                        cost = 100,
                        onBuy = { viewModel.buyPowerUp(PowerUpType.UNDO, 100) }
                    )
                }

                item {
                    PowerUpShopRow(
                        title = "Grid Bomb +1",
                        desc = "Detonate a 3x3 explosive radius",
                        icon = Icons.Default.LocalFireDepartment,
                        cost = 250,
                        onBuy = { viewModel.buyPowerUp(PowerUpType.BOMB, 250) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: ThemeConfig,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) theme.accentColor else Color(0x33FFFFFF),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onSelect)
            .testTag("theme_card_${theme.id.name}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = theme.boardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(theme.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(theme.description, color = Color(0xAAFFFFFF), fontSize = 12.sp)
                }

                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(theme.accentColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("EQUIPPED", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                } else if (isUnlocked) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("EQUIP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("${theme.costCoins} Coins", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Palette preview tiles
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                theme.blockPalette.take(5).forEach { colorData ->
                    SingleBlockCell(colorData = colorData, size = 30.dp)
                }
            }
        }
    }
}

@Composable
fun PowerUpShopRow(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    cost: Int,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = Color(0x88FFFFFF), fontSize = 11.sp)
                }
            }

            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("$cost 🪙", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

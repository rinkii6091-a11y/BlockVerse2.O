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
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeTheme by viewModel.activeTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

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
            // Top Bar
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
                        .testTag("settings_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "SETTINGS",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "AUDIO & HAPTICS",
                        color = activeTheme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                item {
                    SettingToggleRow(
                        title = "Sound Effects",
                        desc = "Procedural snap pops & line clear chimes",
                        icon = Icons.Default.VolumeUp,
                        isChecked = userProfile.soundEnabled,
                        onCheckedChange = { viewModel.toggleSfx() },
                        accentColor = activeTheme.accentColor
                    )
                }

                item {
                    SettingToggleRow(
                        title = "Music & Ambiance",
                        desc = "Futuristic synth ambient layers",
                        icon = Icons.Default.MusicNote,
                        isChecked = userProfile.musicEnabled,
                        onCheckedChange = { viewModel.toggleMusic() },
                        accentColor = activeTheme.accentColor
                    )
                }

                item {
                    SettingToggleRow(
                        title = "Haptic Feedback",
                        desc = "Tactile vibration on snaps and line clears",
                        icon = Icons.Default.Vibration,
                        isChecked = userProfile.hapticsEnabled,
                        onCheckedChange = { viewModel.toggleHaptics() },
                        accentColor = activeTheme.accentColor
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ACCESSIBILITY & MOTION",
                        color = activeTheme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                }

                item {
                    SettingToggleRow(
                        title = "Reduced Motion",
                        desc = "Minimizes particle bursts & heavy camera shakes",
                        icon = Icons.Default.Speed,
                        isChecked = userProfile.reducedMotion,
                        onCheckedChange = { viewModel.toggleReducedMotion() },
                        accentColor = activeTheme.accentColor
                    )
                }

                item {
                    SettingToggleRow(
                        title = "Colorblind Mode",
                        desc = "High-contrast geometric block icons",
                        icon = Icons.Default.Visibility,
                        isChecked = userProfile.colorblindMode,
                        onCheckedChange = { viewModel.toggleColorblindMode() },
                        accentColor = activeTheme.accentColor
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33EF4444)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reset_progress_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFEF4444))
                        Text(
                            text = "RESET PROGRESS",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x221E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("BlockFlow v1.0.0 (2026 Edition)", color = Color(0x88FFFFFF), fontSize = 11.sp)
                            Text("Powered by Kotlin & Jetpack Compose", color = Color(0x66FFFFFF), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset All Progress?", fontWeight = FontWeight.Black) },
                text = { Text("This will reset your High Score, Coins, Gems, and unlocked themes to starting levels.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllProgress()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showResetDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF))
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    desc: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x331E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
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

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0x33FFFFFF)
                )
            )
        }
    }
}

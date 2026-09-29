package com.example.ui.components

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.theme.ThemeConfig

@Composable
fun PauseDialog(
    isOpen: Boolean,
    theme: ThemeConfig,
    isSfxOn: Boolean,
    isMusicOn: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleMusic: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onResume) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(theme.boardBackground)
                .border(2.dp, theme.boardBorder, RoundedCornerShape(28.dp))
                .padding(24.dp)
                .testTag("pause_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PAUSED",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                // Quick audio toggles
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleSfx,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(
                            imageVector = if (isSfxOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle SFX",
                            tint = if (isSfxOn) theme.accentColor else Color.Gray
                        )
                    }

                    IconButton(
                        onClick = onToggleMusic,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(
                            imageVector = if (isMusicOn) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Toggle Music",
                            tint = if (isMusicOn) theme.secondaryAccent else Color.Gray
                        )
                    }
                }

                // Resume Button
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_resume_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }

                // Restart Button
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pause_restart_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Text("RESTART", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }

                // Settings Button
                Button(
                    onClick = onSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                    Text("SETTINGS", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }

                // Home Button
                Button(
                    onClick = onHome,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                    Text("MAIN MENU", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

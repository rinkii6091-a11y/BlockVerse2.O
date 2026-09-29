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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.theme.ThemeConfig

@Composable
fun TutorialOverlay(
    isOpen: Boolean,
    theme: ThemeConfig,
    onGotIt: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onGotIt) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(theme.boardBackground)
                .border(2.dp, theme.accentColor, RoundedCornerShape(28.dp))
                .padding(22.dp)
                .testTag("tutorial_overlay")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TutorialStepRow(
                        icon = Icons.Default.TouchApp,
                        iconTint = theme.accentColor,
                        title = "DRAG & PLACE",
                        desc = "Drag shapes from bottom onto the 8x8 matrix"
                    )

                    TutorialStepRow(
                        icon = Icons.Default.GridView,
                        iconTint = theme.secondaryAccent,
                        title = "CLEAR LINES",
                        desc = "Fill entire rows or columns to shatter blocks"
                    )

                    TutorialStepRow(
                        icon = Icons.Default.ElectricBolt,
                        iconTint = Color(0xFFFFD700),
                        title = "COMBO FLOW",
                        desc = "Clear consecutively to unleash 3X Overdrive"
                    )

                    TutorialStepRow(
                        icon = Icons.Default.AutoAwesome,
                        iconTint = Color(0xFF38BDF8),
                        title = "POWER-UPS",
                        desc = "Use hammers, bombs, and shuffles when grid gets tight"
                    )
                }

                Button(
                    onClick = onGotIt,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tutorial_start_btn")
                ) {
                    Text(
                        text = "LET'S PLAY!",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun TutorialStepRow(icon: ImageVector, iconTint: Color, title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        Column {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color(0xBBFFFFFF), fontSize = 11.sp)
        }
    }
}

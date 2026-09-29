package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.theme.ThemeConfig

@Composable
fun ShareCardDialog(
    isOpen: Boolean,
    score: Int,
    combo: Int,
    lines: Int,
    theme: ThemeConfig,
    onDismiss: () -> Unit
) {
    if (!isOpen) return
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(theme.boardBackground)
                .border(2.dp, theme.accentColor, RoundedCornerShape(28.dp))
                .padding(20.dp)
                .testTag("share_card_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHARE MOMENT",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // The stylized Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E1B4B),
                                    Color(0xFF0F172A),
                                    Color(0xFF0B0F19)
                                )
                            )
                        )
                        .border(1.5.dp, theme.secondaryAccent, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚡ BLOCKFLOW 2026 ⚡",
                            color = theme.accentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = "JUST HIT A ${combo}X COMBO FLOW!",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCORE",
                                color = Color(0x88FFFFFF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$score",
                                color = Color(0xFFFFD700),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("Lines: $lines", color = Color(0xCCFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Theme: ${theme.name}", color = theme.secondaryAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Button(
                    onClick = {
                        shareScoreIntent(context, score, combo, theme.name)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_native_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black)
                    Text("SHARE VIA ANDROID", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

private fun shareScoreIntent(context: Context, score: Int, combo: Int, themeName: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "⚡ I just scored $score points with a ${combo}x Combo in BlockFlow on the $themeName grid! Can you beat my high score? #BlockFlow #Puzzle"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share BlockFlow Score")
    context.startActivity(shareIntent)
}

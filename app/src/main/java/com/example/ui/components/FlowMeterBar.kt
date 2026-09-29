package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MomentumLevel
import com.example.theme.ThemeConfig

@Composable
fun FlowMeterBar(
    flowProgress: Float,
    momentum: MomentumLevel,
    combo: Int,
    theme: ThemeConfig,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = flowProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "flow_meter_progress"
    )

    val momentumColor by animateColorAsState(
        targetValue = Color(momentum.colorHex),
        label = "momentum_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = "Flow",
                    tint = momentumColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = momentum.label,
                    color = momentumColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            if (combo > 1) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x33FF007A))
                        .border(1.dp, Color(0xFFFF007A), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COMBO x$combo",
                        color = Color(0xFFFF4081),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Flow Meter track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x33000000))
                .border(0.8.dp, theme.cellEmptyBorder, RoundedCornerShape(4.dp))
        ) {
            val fillBrush = Brush.horizontalGradient(
                listOf(
                    theme.accentColor,
                    momentumColor,
                    if (momentum == MomentumLevel.OVERDRIVE) Color(0xFFFF007A) else theme.secondaryAccent
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(fillBrush)
                    .then(
                        if (momentum == MomentumLevel.OVERDRIVE) {
                            Modifier.shadow(elevation = (8 * pulseGlow).dp, shape = RoundedCornerShape(4.dp), spotColor = momentumColor)
                        } else Modifier
                    )
            )
        }
    }
}

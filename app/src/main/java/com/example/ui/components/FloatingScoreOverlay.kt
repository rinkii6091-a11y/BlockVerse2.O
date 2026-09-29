package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.model.FloatingScore

@Composable
fun FloatingScoreOverlay(
    floatingScores: List<FloatingScore>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        for (item in floatingScores) {
            FloatingScoreItem(item = item)
        }
    }
}

@Composable
fun FloatingScoreItem(item: FloatingScore) {
    val density = LocalDensity.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(item.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutLinearInEasing)
        )
    }

    val p = progress.value
    val offsetY = with(density) { (item.startY - p * 80f).toInt() }
    val offsetX = with(density) { (item.startX - 60f).toInt() }
    val alpha = (1f - p * 0.8f).coerceIn(0f, 1f)
    val scale = (1f + p * 0.3f)

    Text(
        text = item.text,
        color = item.color,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .scale(scale)
            .alpha(alpha)
    )
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.example.model.Particle

@Composable
fun ParticleOverlay(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    if (particles.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size * p.alpha,
                center = Offset(p.x, p.y)
            )
        }
    }
}

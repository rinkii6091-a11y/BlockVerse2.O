package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.Particle
import com.example.model.ParticleType

/**
 * High-performance 60FPS Canvas Particle Overlay with vibrant neon aesthetics.
 * Renders glowing neon sparks, diamond sparkles, expanding shockwave rings,
 * and high-velocity light streaks triggered upon row and column clears.
 */
@Composable
fun ParticleOverlay(
    particles: List<Particle>,
    modifier: Modifier = Modifier
) {
    if (particles.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in particles) {
            val alpha = p.alpha.coerceIn(0f, 1f)
            if (alpha <= 0.01f) continue

            when (p.type) {
                ParticleType.NEON_CIRCLE -> {
                    drawNeonSpark(p, alpha)
                }
                ParticleType.STAR_SPARKLE -> {
                    drawStarSparkle(p, alpha)
                }
                ParticleType.NEON_RING -> {
                    drawNeonRing(p, alpha)
                }
                ParticleType.LIGHT_STREAK -> {
                    drawLightStreak(p, alpha)
                }
            }
        }
    }
}

/**
 * Draws a multi-layered neon glowing spark with a soft colored halo and a hot white core.
 */
private fun DrawScope.drawNeonSpark(p: Particle, alpha: Float) {
    val center = Offset(p.x, p.y)
    val baseRadius = p.size * alpha

    // 1. Soft Outer Neon Glow
    drawCircle(
        color = p.color.copy(alpha = alpha * 0.35f),
        radius = baseRadius * 2.4f,
        center = center
    )

    // 2. Vibrant Saturated Mid Layer
    drawCircle(
        color = p.color.copy(alpha = alpha * 0.95f),
        radius = baseRadius,
        center = center
    )

    // 3. Hot White Center Specular Core
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.95f),
        radius = baseRadius * 0.42f,
        center = center
    )
}

/**
 * Draws a rotating 4-point neon diamond star sparkle.
 */
private fun DrawScope.drawStarSparkle(p: Particle, alpha: Float) {
    val center = Offset(p.x, p.y)
    val armLength = p.size * 2.2f * alpha
    val armThickness = armLength * 0.28f

    rotate(degrees = p.rotation, pivot = center) {
        // Outer Glow Cross
        drawLine(
            color = p.color.copy(alpha = alpha * 0.45f),
            start = Offset(p.x - armLength * 1.3f, p.y),
            end = Offset(p.x + armLength * 1.3f, p.y),
            strokeWidth = armThickness * 1.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = p.color.copy(alpha = alpha * 0.45f),
            start = Offset(p.x, p.y - armLength * 1.3f),
            end = Offset(p.x, p.y + armLength * 1.3f),
            strokeWidth = armThickness * 1.5f,
            cap = StrokeCap.Round
        )

        // 4-Point Diamond Sparkle Path
        val diamondPath = Path().apply {
            moveTo(p.x, p.y - armLength)
            lineTo(p.x + armThickness, p.y)
            lineTo(p.x, p.y + armLength)
            lineTo(p.x - armThickness, p.y)
            close()
        }
        drawPath(path = diamondPath, color = p.color.copy(alpha = alpha * 0.9f))

        // White Center Core
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = armThickness * 0.75f,
            center = center
        )
    }
}

/**
 * Draws an expanding neon shockwave ring that dissipates as life decreases.
 */
private fun DrawScope.drawNeonRing(p: Particle, alpha: Float) {
    val progress = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
    val currentRadius = p.size * (0.6f + progress * 2.8f)
    val strokeWidth = (3.5f * (1f - progress)).coerceAtLeast(1f)

    // Outer glow ring
    drawCircle(
        color = p.color.copy(alpha = alpha * 0.3f),
        radius = currentRadius + strokeWidth,
        center = Offset(p.x, p.y),
        style = Stroke(width = strokeWidth * 2.2f)
    )

    // Sharp neon ring
    drawCircle(
        color = p.color.copy(alpha = alpha * 0.85f),
        radius = currentRadius,
        center = Offset(p.x, p.y),
        style = Stroke(width = strokeWidth)
    )
}

/**
 * Draws a high-velocity neon laser light streak along its trajectory vector.
 */
private fun DrawScope.drawLightStreak(p: Particle, alpha: Float) {
    val tailLength = (p.length * alpha).coerceAtLeast(16f)
    val angle = kotlin.math.atan2(p.vy.toDouble(), p.vx.toDouble())
    val tailX = p.x - (kotlin.math.cos(angle) * tailLength).toFloat()
    val tailY = p.y - (kotlin.math.sin(angle) * tailLength).toFloat()

    // Outer Glow Streak
    drawLine(
        color = p.color.copy(alpha = alpha * 0.4f),
        start = Offset(tailX, tailY),
        end = Offset(p.x, p.y),
        strokeWidth = p.size * 2.0f,
        cap = StrokeCap.Round
    )

    // Vibrant Core Streak
    drawLine(
        color = p.color.copy(alpha = alpha * 0.9f),
        start = Offset(tailX, tailY),
        end = Offset(p.x, p.y),
        strokeWidth = p.size,
        cap = StrokeCap.Round
    )

    // White Head Dot
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.95f),
        radius = p.size * 0.75f,
        center = Offset(p.x, p.y)
    )
}

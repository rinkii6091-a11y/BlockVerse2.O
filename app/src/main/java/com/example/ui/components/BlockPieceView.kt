package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BlockPiece
import com.example.model.CollectibleType
import com.example.model.SpecialType
import com.example.theme.BlockColorData
import com.example.theme.ThemeConfig

@Composable
fun BlockPieceView(
    piece: BlockPiece,
    theme: ThemeConfig,
    cellSize: Dp = 28.dp,
    modifier: Modifier = Modifier,
    isGhost: Boolean = false,
    alpha: Float = 1.0f
) {
    val colorData = theme.blockPalette.getOrElse(piece.colorIndex) { theme.blockPalette[0] }

    Column(modifier = modifier) {
        piece.shapeMatrix.forEachIndexed { r, row ->
            Row {
                row.forEachIndexed { c, isFilled ->
                    if (isFilled) {
                        SingleBlockCell(
                            colorData = colorData,
                            specialType = piece.specialType,
                            collectible = piece.collectible,
                            size = cellSize,
                            isGhost = isGhost,
                            alpha = alpha
                        )
                    } else {
                        Box(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}

@Composable
fun SingleBlockCell(
    colorData: BlockColorData,
    specialType: SpecialType = SpecialType.NONE,
    collectible: CollectibleType = CollectibleType.NONE,
    size: Dp = 38.dp,
    isGhost: Boolean = false,
    alpha: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isGhost) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cell_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * scale)) {
            val w = this.size.width
            val h = this.size.height
            val cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)

            if (isGhost) {
                // Ghost translucent snap preview
                drawRoundRect(
                    color = colorData.mainColor.copy(alpha = 0.45f * alpha),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = cornerRadius
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.7f * alpha),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = cornerRadius,
                    style = Stroke(width = w * 0.08f)
                )
                return@Canvas
            }

            // Outer Soft Glow Shadow
            drawRoundRect(
                color = colorData.glowColor.copy(alpha = 0.35f * alpha),
                topLeft = Offset(0f, h * 0.08f),
                size = Size(w, h),
                cornerRadius = cornerRadius
            )

            // Main 3D Gradient Surface
            val mainGradient = Brush.verticalGradient(
                colors = listOf(
                    colorData.topHighlight.copy(alpha = alpha),
                    colorData.mainColor.copy(alpha = alpha),
                    colorData.bottomShadow.copy(alpha = alpha)
                )
            )
            drawRoundRect(
                brush = mainGradient,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = cornerRadius
            )

            // Glossy Top Bevel / Specular Reflection
            val glossGradient = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.65f * alpha),
                    Color.White.copy(alpha = 0.05f * alpha)
                ),
                startY = 0f,
                endY = h * 0.45f
            )
            drawRoundRect(
                brush = glossGradient,
                topLeft = Offset(w * 0.10f, h * 0.08f),
                size = Size(w * 0.80f, h * 0.35f),
                cornerRadius = CornerRadius(w * 0.15f, h * 0.15f)
            )

            // Subtle crisp border outline
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f * alpha),
                topLeft = Offset(0.5f, 0.5f),
                size = Size(w - 1f, h - 1f),
                cornerRadius = cornerRadius,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // Special Badges overlay
        when (specialType) {
            SpecialType.BOMB -> {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Bomb Block",
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(size * 0.62f)
                )
            }
            SpecialType.LIGHTNING_H, SpecialType.LIGHTNING_V -> {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Lightning Block",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.62f)
                )
            }
            SpecialType.RAINBOW, SpecialType.PRISM -> {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Prism Block",
                    tint = Color(0xFFE0F7FA),
                    modifier = Modifier.size(size * 0.58f)
                )
            }
            else -> {}
        }

        // Collectibles Overlay
        when (collectible) {
            CollectibleType.STAR -> {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            CollectibleType.GEM -> {
                Icon(
                    imageVector = Icons.Default.Diamond,
                    contentDescription = "Gem",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(size * 0.55f)
                )
            }
            CollectibleType.COIN -> {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Coin",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(size * 0.50f)
                )
            }
            else -> {}
        }
    }
}

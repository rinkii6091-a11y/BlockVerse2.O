package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BlockPiece
import com.example.model.CellState
import com.example.model.PowerUpType
import com.example.theme.ThemeConfig

@Composable
fun BoardView(
    board: List<List<CellState>>,
    theme: ThemeConfig,
    draggingPiece: BlockPiece?,
    hoverPosition: Pair<Int, Int>?,
    isPlacementValid: Boolean,
    activePowerUp: PowerUpType?,
    onCellClicked: (Int, Int) -> Unit,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Ghost map: set of (r, c) occupied by currently hovered piece
    val ghostCells = remember(draggingPiece, hoverPosition, isPlacementValid) {
        val map = mutableMapOf<Pair<Int, Int>, Boolean>()
        if (draggingPiece != null && hoverPosition != null) {
            val (startR, startC) = hoverPosition
            for (r in 0 until draggingPiece.rows) {
                for (c in 0 until draggingPiece.cols) {
                    if (draggingPiece.shapeMatrix[r][c]) {
                        val br = startR + r
                        val bc = startC + c
                        if (br in 0..7 && bc in 0..7) {
                            map[br to bc] = isPlacementValid
                        }
                    }
                }
            }
        }
        map
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .aspectRatio(1f)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = theme.accentColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(theme.boardBackground)
            .border(
                width = 2.dp,
                color = if (activePowerUp != null) theme.secondaryAccent else theme.boardBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(10.dp)
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInRoot()
                val innerWidth = bounds.width - with(density) { 20.dp.toPx() }
                val cellSize = innerWidth / 8f
                onBoundsChanged(bounds, cellSize)
            }
            .testTag("game_board_grid"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cellSizeDp = maxWidth / 8f

            Column(modifier = Modifier.fillMaxSize()) {
                for (r in 0 until 8) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0 until 8) {
                            val cell = board[r][c]
                            val isGhost = ghostCells.containsKey(r to c)
                            val ghostValid = ghostCells[r to c] ?: false

                            Box(
                                modifier = Modifier
                                    .size(cellSizeDp)
                                    .padding(2.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = activePowerUp != null
                                    ) {
                                        onCellClicked(r, c)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell.isFilled) {
                                    val colorData = theme.blockPalette.getOrElse(cell.colorIndex) { theme.blockPalette[0] }
                                    SingleBlockCell(
                                        colorData = colorData,
                                        specialType = cell.specialType,
                                        collectible = cell.collectible,
                                        size = cellSizeDp - 4.dp
                                    )
                                } else if (isGhost) {
                                    // Ghost piece preview
                                    val ghostColorData = if (ghostValid && draggingPiece != null) {
                                        theme.blockPalette.getOrElse(draggingPiece.colorIndex) { theme.blockPalette[0] }
                                    } else {
                                        theme.blockPalette[0]
                                    }
                                    SingleBlockCell(
                                        colorData = ghostColorData,
                                        size = cellSizeDp - 4.dp,
                                        isGhost = true,
                                        alpha = if (ghostValid) 0.85f else 0.45f
                                    )
                                } else {
                                    // Empty sleek recessed grid slot
                                    EmptyCellSlot(
                                        theme = theme,
                                        isTargeted = activePowerUp != null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCellSlot(
    theme: ThemeConfig,
    isTargeted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val corner = CornerRadius(w * 0.18f, h * 0.18f)

        // Subtle recessed background
        drawRoundRect(
            color = theme.cellEmptyBackground,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = corner
        )

        // Subtle thin bevel border
        drawRoundRect(
            color = if (isTargeted) theme.accentColor.copy(alpha = 0.5f) else theme.cellEmptyBorder,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = corner,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

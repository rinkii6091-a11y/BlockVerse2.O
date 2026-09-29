package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.BlockPiece
import com.example.theme.ThemeConfig

@Composable
fun PieceTray(
    availablePieces: List<BlockPiece?>,
    theme: ThemeConfig,
    activeDraggingIndex: Int?,
    selectedPieceIndex: Int? = null,
    onDragStart: (Int, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onPieceTap: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (i in 0 until 3) {
            val piece = availablePieces.getOrNull(i)
            val isBeingDragged = activeDraggingIndex == i
            val isSelectedForTapPlace = selectedPieceIndex == i

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .scale(if (isSelectedForTapPlace) pulseScale else 1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isSelectedForTapPlace) theme.accentColor.copy(alpha = 0.22f)
                        else theme.boardBackground.copy(alpha = 0.65f)
                    )
                    .border(
                        width = if (isSelectedForTapPlace) 2.2.dp else 1.2.dp,
                        color = if (isSelectedForTapPlace) theme.accentColor else theme.cellEmptyBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onPieceTap?.invoke(i)
                    }
                    .padding(6.dp)
                    .testTag("piece_slot_$i"),
                contentAlignment = Alignment.Center
            ) {
                if (piece != null) {
                    var slotCenterInRoot = Offset.Zero

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (isBeingDragged) 0.15f else 1.0f)
                            .onGloballyPositioned { coords ->
                                val bounds = coords.boundsInRoot()
                                slotCenterInRoot = bounds.center
                            }
                            .pointerInput(piece.id) {
                                detectDragGestures(
                                    onDragStart = { localOffset ->
                                        // Pass initial touch position in root coordinate space
                                        val rootTouch = slotCenterInRoot + localOffset
                                        onDragStart(i, rootTouch)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDrag(dragAmount)
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragCancel() }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        BlockPieceView(
                            piece = piece,
                            theme = theme,
                            cellSize = 20.dp
                        )
                    }
                }
            }
        }
    }
}

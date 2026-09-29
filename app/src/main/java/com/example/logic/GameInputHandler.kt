package com.example.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.model.BlockPiece
import com.example.model.CellState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Result of attempting to place a piece via drag-and-drop or tap-to-place.
 */
sealed class PlacementResult {
    data class Success(
        val pieceIndex: Int,
        val piece: BlockPiece,
        val targetRow: Int,
        val targetCol: Int
    ) : PlacementResult()

    data class Invalid(
        val pieceIndex: Int,
        val piece: BlockPiece,
        val attemptedRow: Int,
        val attemptedCol: Int
    ) : PlacementResult()

    object Cancelled : PlacementResult()
    object None : PlacementResult()
}

/**
 * Snapshot of current input and hover state managed by [GameInputHandler].
 */
data class InputHandlerState(
    val activePieceIndex: Int? = null,
    val activePiece: BlockPiece? = null,
    val dragPosition: Offset = Offset.Zero,
    val hoverPosition: Pair<Int, Int>? = null,
    val isPlacementValid: Boolean = false,
    val isTapSelected: Boolean = false,
    val ghostCells: Map<Pair<Int, Int>, Boolean> = emptyMap()
)

/**
 * Touch and Gesture Input Handler that translates screen touches (drag-and-drop or tap-to-place)
 * into coordinate updates for the [GameLogic] class.
 *
 * Responsibilities:
 * 1. Screen-to-Grid Coordinate Translation with thumb-obscurity offset compensation.
 * 2. Real-time spatial validation with [GameLogic.canPlacePiece].
 * 3. Ghost preview computation for the 8x8 grid.
 * 4. Tap-to-Place targeting: intelligently aligns tapped cells with piece shapes.
 * 5. Drag-and-drop gesture lifecycle management.
 */
class GameInputHandler(
    val gameLogic: GameLogic = GameLogic.instance
) {
    private val _state = MutableStateFlow(InputHandlerState())
    val state: StateFlow<InputHandlerState> = _state.asStateFlow()

    /**
     * Translates a raw screen touch [Offset] into a candidate top-left grid coordinate `(row, col)`
     * on the board, compensating for thumb occlusion.
     */
    fun translateTouchToGrid(
        touchOffset: Offset,
        boardBounds: Rect,
        cellSizePx: Float,
        piece: BlockPiece,
        visualYOffset: Float = cellSizePx * 1.5f
    ): Pair<Int, Int>? {
        if (boardBounds.width <= 0 || cellSizePx <= 0) return null

        // Offset piece center above finger
        val pieceWidthPx = piece.cols * cellSizePx
        val pieceHeightPx = piece.rows * cellSizePx

        val adjustedX = touchOffset.x - (pieceWidthPx / 2f)
        val adjustedY = (touchOffset.y - visualYOffset) - (pieceHeightPx / 2f)

        val localX = adjustedX - boardBounds.left
        val localY = adjustedY - boardBounds.top

        val maxCol = (gameLogic.boardSize - piece.cols).coerceAtLeast(0)
        val maxRow = (gameLogic.boardSize - piece.rows).coerceAtLeast(0)

        val col = Math.round(localX / cellSizePx).toInt().coerceIn(0, maxCol)
        val row = Math.round(localY / cellSizePx).toInt().coerceIn(0, maxRow)

        return row to col
    }

    /**
     * Computes the map of ghost cell coordinates and their validity state.
     */
    fun computeGhostCells(
        piece: BlockPiece,
        startRow: Int,
        startCol: Int,
        isValid: Boolean
    ): Map<Pair<Int, Int>, Boolean> {
        val ghostMap = mutableMapOf<Pair<Int, Int>, Boolean>()
        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shapeMatrix[r][c]) {
                    val br = startRow + r
                    val bc = startCol + c
                    if (gameLogic.isWithinBounds(br, bc)) {
                        ghostMap[br to bc] = isValid
                    }
                }
            }
        }
        return ghostMap
    }

    // ==========================================
    // DRAG-AND-DROP LIFECYCLE
    // ==========================================

    /**
     * Initiates a drag gesture for the piece at [pieceIndex].
     */
    fun onDragStart(
        pieceIndex: Int,
        piece: BlockPiece,
        screenTouch: Offset,
        boardBounds: Rect,
        cellSizePx: Float,
        board: List<List<CellState>>
    ): InputHandlerState {
        val gridPos = translateTouchToGrid(screenTouch, boardBounds, cellSizePx, piece)
        val isValid = gridPos != null && gameLogic.canPlacePiece(board, piece, gridPos.first, gridPos.second)
        val ghosts = if (gridPos != null) computeGhostCells(piece, gridPos.first, gridPos.second, isValid) else emptyMap()

        val newState = InputHandlerState(
            activePieceIndex = pieceIndex,
            activePiece = piece,
            dragPosition = screenTouch,
            hoverPosition = gridPos,
            isPlacementValid = isValid,
            isTapSelected = false,
            ghostCells = ghosts
        )
        _state.value = newState
        return newState
    }

    /**
     * Updates drag position and translates delta into live grid coordinates.
     */
    fun onDrag(
        dragDelta: Offset,
        boardBounds: Rect,
        cellSizePx: Float,
        board: List<List<CellState>>
    ): InputHandlerState {
        val current = _state.value
        val piece = current.activePiece ?: return current

        val newTouch = current.dragPosition + dragDelta
        val gridPos = translateTouchToGrid(newTouch, boardBounds, cellSizePx, piece)
        val isValid = gridPos != null && gameLogic.canPlacePiece(board, piece, gridPos.first, gridPos.second)
        val ghosts = if (gridPos != null) computeGhostCells(piece, gridPos.first, gridPos.second, isValid) else emptyMap()

        val newState = current.copy(
            dragPosition = newTouch,
            hoverPosition = gridPos,
            isPlacementValid = isValid,
            ghostCells = ghosts
        )
        _state.value = newState
        return newState
    }

    /**
     * Completes a drag gesture. Validates the candidate position with [GameLogic]
     * and returns a [PlacementResult].
     */
    fun onDragEnd(board: List<List<CellState>>): PlacementResult {
        val current = _state.value
        val idx = current.activePieceIndex
        val piece = current.activePiece
        val hover = current.hoverPosition

        val result = if (idx != null && piece != null && hover != null && current.isPlacementValid) {
            val (r, c) = hover
            if (gameLogic.canPlacePiece(board, piece, r, c)) {
                PlacementResult.Success(idx, piece, r, c)
            } else {
                PlacementResult.Invalid(idx, piece, r, c)
            }
        } else if (idx != null && piece != null) {
            val (r, c) = hover ?: (0 to 0)
            PlacementResult.Invalid(idx, piece, r, c)
        } else {
            PlacementResult.Cancelled
        }

        // Reset state
        _state.value = InputHandlerState()
        return result
    }

    /**
     * Cancels an ongoing drag gesture.
     */
    fun onDragCancel(): InputHandlerState {
        val resetState = InputHandlerState()
        _state.value = resetState
        return resetState
    }

    // ==========================================
    // TAP-TO-PLACE LIFECYCLE
    // ==========================================

    /**
     * Handles tapping a piece slot in the piece tray to toggle selection for tap-to-place.
     */
    fun onPieceTrayTapped(pieceIndex: Int, piece: BlockPiece): InputHandlerState {
        val current = _state.value
        val isAlreadySelected = current.activePieceIndex == pieceIndex && current.isTapSelected

        val newState = if (isAlreadySelected) {
            // Deselect
            InputHandlerState()
        } else {
            // Select this piece for tap-to-place
            InputHandlerState(
                activePieceIndex = pieceIndex,
                activePiece = piece,
                isTapSelected = true
            )
        }
        _state.value = newState
        return newState
    }

    /**
     * Resolves a board cell tap into the most natural placement for the currently selected piece.
     * Searches for a valid placement where the tapped cell is part of the piece shape.
     */
    fun findPlacementForCellTap(
        tappedRow: Int,
        tappedCol: Int,
        piece: BlockPiece,
        board: List<List<CellState>>
    ): Pair<Int, Int>? {
        // 1. Direct placement starting at (tappedRow, tappedCol)
        if (gameLogic.canPlacePiece(board, piece, tappedRow, tappedCol)) {
            return tappedRow to tappedCol
        }

        // 2. Centered placement over (tappedRow, tappedCol)
        val centerStartR = (tappedRow - piece.rows / 2).coerceIn(0, (gameLogic.boardSize - piece.rows).coerceAtLeast(0))
        val centerStartC = (tappedCol - piece.cols / 2).coerceIn(0, (gameLogic.boardSize - piece.cols).coerceAtLeast(0))
        if (gameLogic.canPlacePiece(board, piece, centerStartR, centerStartC)) {
            return centerStartR to centerStartC
        }

        // 3. Scan all valid anchorings where the tapped cell coincides with a block in the piece
        for (dr in 0 until piece.rows) {
            for (dc in 0 until piece.cols) {
                if (piece.shapeMatrix[dr][dc]) {
                    val candidateR = tappedRow - dr
                    val candidateC = tappedCol - dc
                    if (candidateR in 0..(gameLogic.boardSize - piece.rows) &&
                        candidateC in 0..(gameLogic.boardSize - piece.cols)
                    ) {
                        if (gameLogic.canPlacePiece(board, piece, candidateR, candidateC)) {
                            return candidateR to candidateC
                        }
                    }
                }
            }
        }

        return null
    }

    /**
     * Handles tapping a cell on the board during Tap-to-Place mode.
     */
    fun onBoardCellTapped(
        row: Int,
        col: Int,
        board: List<List<CellState>>
    ): PlacementResult {
        val current = _state.value
        val idx = current.activePieceIndex
        val piece = current.activePiece

        if (idx == null || piece == null || !current.isTapSelected) {
            return PlacementResult.None
        }

        val targetAnchor = findPlacementForCellTap(row, col, piece, board)
        return if (targetAnchor != null) {
            val (tr, tc) = targetAnchor
            _state.value = InputHandlerState()
            PlacementResult.Success(idx, piece, tr, tc)
        } else {
            PlacementResult.Invalid(idx, piece, row, col)
        }
    }

    /**
     * Clears any active selection or drag state.
     */
    fun clearSelection() {
        _state.value = InputHandlerState()
    }
}

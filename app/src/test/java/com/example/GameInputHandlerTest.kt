package com.example

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.logic.GameInputHandler
import com.example.logic.GameLogic
import com.example.logic.PlacementResult
import com.example.model.BlockPiece
import com.example.model.CellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GameInputHandler].
 * Verifies screen touch to grid coordinate translations, drag-and-drop lifecycle,
 * and tap-to-place coordinate updates for [GameLogic].
 */
class GameInputHandlerTest {

    private lateinit var gameLogic: GameLogic
    private lateinit var inputHandler: GameInputHandler

    private val cellSizePx = 50f
    private val boardBounds = Rect(left = 0f, top = 100f, right = 400f, bottom = 500f)

    @Before
    fun setUp() {
        gameLogic = GameLogic(boardSize = 8)
        inputHandler = GameInputHandler(gameLogic)
    }

    // ==========================================
    // 1. COORDINATE TRANSLATION TESTS
    // ==========================================

    @Test
    fun translateTouchToGrid_translatesCoordinatesAccurately() {
        val dotPiece = BlockPiece(
            id = "dot",
            shapeMatrix = listOf(listOf(true)),
            colorIndex = 1
        )

        // Touch at top-left cell center (local: x=25, y=25 + visualYOffset)
        // visualYOffset = 50 * 1.5 = 75f
        // screen touch: x = 25, y = 100 + 25 + 75 = 200f
        val touchOffset = Offset(x = 25f, y = 200f)
        val gridPos = inputHandler.translateTouchToGrid(
            touchOffset = touchOffset,
            boardBounds = boardBounds,
            cellSizePx = cellSizePx,
            piece = dotPiece
        )

        assertNotNull(gridPos)
        assertEquals(0, gridPos!!.first)
        assertEquals(0, gridPos.second)
    }

    @Test
    fun translateTouchToGrid_clampsWithinBoardBoundaries() {
        val piece2x2 = BlockPiece(
            id = "2x2",
            shapeMatrix = listOf(listOf(true, true), listOf(true, true)),
            colorIndex = 1
        )

        // Far beyond bottom right
        val farTouch = Offset(x = 9999f, y = 9999f)
        val clampedPos = inputHandler.translateTouchToGrid(
            touchOffset = farTouch,
            boardBounds = boardBounds,
            cellSizePx = cellSizePx,
            piece = piece2x2
        )

        assertNotNull(clampedPos)
        // Since boardSize = 8 and piece.rows = 2, maxRow = 6, maxCol = 6
        assertEquals(6, clampedPos!!.first)
        assertEquals(6, clampedPos.second)
    }

    // ==========================================
    // 2. DRAG-AND-DROP LIFECYCLE TESTS
    // ==========================================

    @Test
    fun dragLifecycle_validDropReturnsPlacementSuccess() {
        val board = gameLogic.createEmptyBoard()
        val piece = BlockPiece(
            id = "test_piece",
            shapeMatrix = listOf(listOf(true, true)),
            colorIndex = 2
        )

        // Start drag at cell (0, 0)
        val initialTouch = Offset(x = 50f, y = 200f)
        val startState = inputHandler.onDragStart(
            pieceIndex = 0,
            piece = piece,
            screenTouch = initialTouch,
            boardBounds = boardBounds,
            cellSizePx = cellSizePx,
            board = board
        )

        assertEquals(0, startState.activePieceIndex)
        assertTrue(startState.isPlacementValid)

        // Complete drop
        val result = inputHandler.onDragEnd(board)
        assertTrue("Result should be Success", result is PlacementResult.Success)
        val success = result as PlacementResult.Success
        assertEquals(0, success.pieceIndex)
        assertEquals(piece.id, success.piece.id)
    }

    @Test
    fun dragLifecycle_obstructedDropReturnsPlacementInvalid() {
        // Obstruct cell (0, 0)
        val board = List(8) { r ->
            List(8) { c ->
                CellState(isFilled = (r == 0 && c == 0))
            }
        }
        val piece = BlockPiece(
            id = "test_piece",
            shapeMatrix = listOf(listOf(true)),
            colorIndex = 2
        )

        // Drag over obstructed (0, 0)
        val touch = Offset(x = 25f, y = 200f)
        inputHandler.onDragStart(
            pieceIndex = 0,
            piece = piece,
            screenTouch = touch,
            boardBounds = boardBounds,
            cellSizePx = cellSizePx,
            board = board
        )

        val result = inputHandler.onDragEnd(board)
        assertTrue("Result should be Invalid", result is PlacementResult.Invalid)
    }

    // ==========================================
    // 3. TAP-TO-PLACE LIFECYCLE TESTS
    // ==========================================

    @Test
    fun tapToPlace_pieceSelectionTogglesCorrectly() {
        val piece = BlockPiece("p1", listOf(listOf(true)), 1)

        val state1 = inputHandler.onPieceTrayTapped(pieceIndex = 1, piece = piece)
        assertTrue(state1.isTapSelected)
        assertEquals(1, state1.activePieceIndex)

        // Tapping same slot toggles off (deselects)
        val state2 = inputHandler.onPieceTrayTapped(pieceIndex = 1, piece = piece)
        assertFalse(state2.isTapSelected)
        assertNull(state2.activePieceIndex)
    }

    @Test
    fun tapToPlace_boardCellTapResolvesPlacementCoordinate() {
        val board = gameLogic.createEmptyBoard()
        val piece = BlockPiece(
            id = "line3",
            shapeMatrix = listOf(listOf(true, true, true)),
            colorIndex = 4
        )

        // Select piece for tap-to-place
        inputHandler.onPieceTrayTapped(pieceIndex = 2, piece = piece)

        // Tap cell (2, 2) on the board
        val result = inputHandler.onBoardCellTapped(row = 2, col = 2, board = board)
        assertTrue("Result must be PlacementResult.Success", result is PlacementResult.Success)

        val success = result as PlacementResult.Success
        assertEquals(2, success.pieceIndex)
        // Placement must be legal according to GameLogic
        assertTrue(gameLogic.canPlacePiece(board, piece, success.targetRow, success.targetCol))
    }

    @Test
    fun tapToPlace_returnsNoneWhenNoPieceSelected() {
        val board = gameLogic.createEmptyBoard()
        inputHandler.clearSelection()

        val result = inputHandler.onBoardCellTapped(row = 3, col = 3, board = board)
        assertEquals(PlacementResult.None, result)
    }
}

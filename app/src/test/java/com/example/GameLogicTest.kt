package com.example

import com.example.logic.GameLogic
import com.example.model.BlockPiece
import com.example.model.CellState
import com.example.model.CollectibleType
import com.example.model.SpecialType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GameLogic].
 * Verifies piece generation, board occupancy, row/column clearing, and combo multipliers.
 */
class GameLogicTest {

    private lateinit var gameLogic: GameLogic

    @Before
    fun setUp() {
        gameLogic = GameLogic(boardSize = 8)
    }

    // ==========================================
    // 1. BOARD OCCUPANCY & PLACEMENT TESTS
    // ==========================================

    @Test
    fun createEmptyBoard_hasCorrectDimensionsAndEmptyCells() {
        val board = gameLogic.createEmptyBoard()
        assertEquals(8, board.size)
        for (row in board) {
            assertEquals(8, row.size)
            for (cell in row) {
                assertFalse(cell.isFilled)
            }
        }
    }

    @Test
    fun canPlacePiece_validOnEmptyBoard() {
        val board = gameLogic.createEmptyBoard()
        val piece = BlockPiece(
            id = "test_2x2",
            shapeMatrix = listOf(listOf(true, true), listOf(true, true)),
            colorIndex = 1
        )

        assertTrue(gameLogic.canPlacePiece(board, piece, startRow = 0, startCol = 0))
        assertTrue(gameLogic.canPlacePiece(board, piece, startRow = 6, startCol = 6))
        // Out of bounds
        assertFalse(gameLogic.canPlacePiece(board, piece, startRow = 7, startCol = 7))
        assertFalse(gameLogic.canPlacePiece(board, piece, startRow = -1, startCol = 0))
    }

    @Test
    fun stampPiece_correctlyUpdatesOccupancy() {
        val board = gameLogic.createEmptyBoard()
        val piece = BlockPiece(
            id = "test_dot",
            shapeMatrix = listOf(listOf(true)),
            colorIndex = 3,
            collectible = CollectibleType.COIN
        )

        val updated = gameLogic.stampPiece(board, piece, startRow = 2, startCol = 3)
        assertTrue(updated[2][3].isFilled)
        assertEquals(3, updated[2][3].colorIndex)
        assertEquals(CollectibleType.COIN, updated[2][3].collectible)

        val occupancy = gameLogic.calculateOccupancy(updated)
        assertEquals(1, occupancy.filledCells)
        assertEquals(64, occupancy.totalCells)
        assertEquals(1f / 64f, occupancy.occupancyRatio, 0.001f)
        assertFalse(occupancy.isDangerZone)
    }

    @Test
    fun canPlacePiece_returnsFalseWhenObstructed() {
        val board = gameLogic.createEmptyBoard()
        val dot = BlockPiece("dot", listOf(listOf(true)), colorIndex = 0)
        val stampedBoard = gameLogic.stampPiece(board, dot, startRow = 2, startCol = 2)

        val piece2x2 = BlockPiece("square", listOf(listOf(true, true), listOf(true, true)), colorIndex = 1)
        // Obstructed at (2, 2)
        assertFalse(gameLogic.canPlacePiece(stampedBoard, piece2x2, startRow = 1, startCol = 1))
        // Free at (4, 4)
        assertTrue(gameLogic.canPlacePiece(stampedBoard, piece2x2, startRow = 4, startCol = 4))
    }

    // ==========================================
    // 2. PIECE GENERATION TESTS
    // ==========================================

    @Test
    fun generatePieceTrio_generatesExactlyThreePieces() {
        val board = gameLogic.createEmptyBoard()
        val trio = gameLogic.generatePieceTrio(board, playerLevel = 1)

        assertEquals(3, trio.size)
        for (piece in trio) {
            assertNotNull(piece)
            assertTrue(piece.rows > 0)
            assertTrue(piece.cols > 0)
            assertTrue(piece.blockCount > 0)
        }
    }

    @Test
    fun generatePieceTrio_guaranteesAtLeastOnePieceCanFit() {
        // Create an almost full board with only a 1x1 hole at (0, 0)
        val almostFull = List(8) { r ->
            List(8) { c ->
                CellState(isFilled = !(r == 0 && c == 0))
            }
        }

        val trio = gameLogic.generatePieceTrio(almostFull, playerLevel = 1)
        assertTrue("At least one piece must be able to fit", gameLogic.canAnyPieceFit(almostFull, trio))
    }

    // ==========================================
    // 3. ROW & COLUMN CLEARING TESTS
    // ==========================================

    @Test
    fun detectAndClear_clearsFullRow() {
        // Fill row 3 completely
        val board = List(8) { r ->
            List(8) { c ->
                CellState(isFilled = (r == 3))
            }
        }

        val fullRows = gameLogic.detectFullRows(board)
        assertEquals(listOf(3), fullRows)
        assertTrue(gameLogic.detectFullCols(board).isEmpty())

        val clearResult = gameLogic.processLineClears(board, fullRows, emptyList())
        assertEquals(1, clearResult.totalLinesCleared)
        assertEquals(8, clearResult.clearedCells.size)

        // Verify row 3 is now completely empty
        for (c in 0 until 8) {
            assertFalse(clearResult.updatedBoard[3][c].isFilled)
        }
    }

    @Test
    fun detectAndClear_withBombSpecialBlock_clearsBlastRadius() {
        // Fill row 0 with a Bomb special block at (0, 4)
        val board = List(8) { r ->
            List(8) { c ->
                if (r == 0) {
                    if (c == 4) CellState(isFilled = true, specialType = SpecialType.BOMB)
                    else CellState(isFilled = true)
                } else if (r == 1 && c == 4) {
                    CellState(isFilled = true) // In blast radius of (0, 4)
                } else {
                    CellState(isFilled = false)
                }
            }
        }

        val fullRows = gameLogic.detectFullRows(board)
        val result = gameLogic.processLineClears(board, fullRows, emptyList())

        assertTrue(result.specialEffectsTriggered.any { it.specialType == SpecialType.BOMB })
        // (1, 4) should be cleared by the blast even though row 1 wasn't a full line!
        assertFalse(result.updatedBoard[1][4].isFilled)
    }

    // ==========================================
    // 4. COMBO MULTIPLIERS & SCORING TESTS
    // ==========================================

    @Test
    fun calculateComboMultiplier_scalesCorrectly() {
        assertEquals(1.0f, gameLogic.calculateComboMultiplier(combo = 1, momentumMultiplier = 1.0f), 0.001f)
        assertEquals(1.5f, gameLogic.calculateComboMultiplier(combo = 2, momentumMultiplier = 1.0f), 0.001f)
        assertEquals(2.0f, gameLogic.calculateComboMultiplier(combo = 3, momentumMultiplier = 1.0f), 0.001f)
        assertEquals(2.5f, gameLogic.calculateComboMultiplier(combo = 4, momentumMultiplier = 1.0f), 0.001f)
        assertEquals(3.0f, gameLogic.calculateComboMultiplier(combo = 5, momentumMultiplier = 1.0f), 0.001f)

        // Momentum scaling
        assertEquals(3.0f, gameLogic.calculateComboMultiplier(combo = 2, momentumMultiplier = 2.0f), 0.001f)
    }

    @Test
    fun calculateLineClearScore_awardsCorrectBaseAndComboPoints() {
        // 1 line, combo 1
        val score1 = gameLogic.calculateLineClearScore(linesCleared = 1, combo = 1)
        assertEquals(100, score1.baseLineScore)
        assertEquals(0, score1.comboBonus)
        assertEquals(100, score1.totalScoreEarned)
        assertEquals("LINE CLEAR!", score1.bannerText)

        // 2 lines, combo 2
        val score2 = gameLogic.calculateLineClearScore(linesCleared = 2, combo = 2)
        assertEquals(300, score2.baseLineScore)
        assertEquals(150, score2.comboBonus)
        assertEquals(450, score2.totalScoreEarned)
        assertEquals("2X FLOW!", score2.bannerText)

        // 4 lines (Quad clear!), combo 3, hype momentum (2.0x)
        val score4 = gameLogic.calculateLineClearScore(linesCleared = 4, combo = 3, momentumMultiplier = 2.0f)
        assertEquals(1000, score4.baseLineScore)
        assertEquals(300, score4.comboBonus)
        assertEquals((1300 * 2.0f).toInt(), score4.totalScoreEarned)
        assertEquals("PERFECT COMBO!", score4.bannerText)
    }
}

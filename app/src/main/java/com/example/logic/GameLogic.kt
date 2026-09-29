package com.example.logic

import com.example.model.BlockPiece
import com.example.model.CellState
import com.example.model.CollectibleType
import com.example.model.SpecialType
import java.util.UUID

/**
 * Event representing a special block effect triggered during line clears.
 */
data class SpecialEffectEvent(
    val specialType: SpecialType,
    val originRow: Int,
    val originCol: Int,
    val affectedCells: Set<Pair<Int, Int>>
)

/**
 * Event representing an in-game collectible gathered during line clears.
 */
data class CollectibleEvent(
    val collectibleType: CollectibleType,
    val row: Int,
    val col: Int
)

/**
 * Snapshot of board occupancy metrics.
 */
data class BoardOccupancy(
    val filledCells: Int,
    val totalCells: Int = 64,
    val occupancyRatio: Float,
    val isDangerZone: Boolean
)

/**
 * Result details from executing a row/column clear operation.
 */
data class ClearResult(
    val updatedBoard: List<List<CellState>>,
    val fullRows: List<Int>,
    val fullCols: List<Int>,
    val totalLinesCleared: Int,
    val clearedCells: Set<Pair<Int, Int>>,
    val specialEffectsTriggered: List<SpecialEffectEvent>,
    val collectiblesGathered: List<CollectibleEvent>
)

/**
 * Comprehensive score breakdown calculated for placement or line clearing.
 */
data class ScoreBreakdown(
    val baseLineScore: Int,
    val comboBonus: Int,
    val comboMultiplier: Float,
    val totalScoreEarned: Int,
    val bannerText: String
)

/**
 * Core Game Engine Logic class for BlockFlow.
 *
 * Encapsulates:
 * 1. Piece Generation (Smart trios, fairness validation, special blocks)
 * 2. Board Occupancy (Bounds verification, placement validity, spatial stats)
 * 3. Row & Column Clearing (Full line detection, secondary blast propagation, ice shattering)
 * 4. Combo Multipliers & Scoring (Multi-line bonuses, streak multipliers, hype titles)
 */
class GameLogic(val boardSize: Int = BOARD_SIZE) {

    companion object {
        const val BOARD_SIZE = 8

        // Default shared singleton instance
        val instance = GameLogic()

        /**
         * Standard catalog of polyomino shapes used in BlockFlow.
         */
        val STANDARD_SHAPES: List<List<List<Boolean>>> = listOf(
            // 1x1 Single Dot
            listOf(listOf(true)),

            // Lines 2
            listOf(listOf(true, true)),
            listOf(listOf(true), listOf(true)),

            // Lines 3
            listOf(listOf(true, true, true)),
            listOf(listOf(true), listOf(true), listOf(true)),

            // Lines 4
            listOf(listOf(true, true, true, true)),
            listOf(listOf(true), listOf(true), listOf(true), listOf(true)),

            // Lines 5
            listOf(listOf(true, true, true, true, true)),
            listOf(listOf(true), listOf(true), listOf(true), listOf(true), listOf(true)),

            // 2x2 Square
            listOf(
                listOf(true, true),
                listOf(true, true)
            ),

            // 3x3 Giant Square
            listOf(
                listOf(true, true, true),
                listOf(true, true, true),
                listOf(true, true, true)
            ),

            // Small Corners 2x2
            listOf(
                listOf(true, true),
                listOf(true, false)
            ),
            listOf(
                listOf(true, true),
                listOf(false, true)
            ),
            listOf(
                listOf(true, false),
                listOf(true, true)
            ),
            listOf(
                listOf(false, true),
                listOf(true, true)
            ),

            // L-Shape 3x2
            listOf(
                listOf(true, false),
                listOf(true, false),
                listOf(true, true)
            ),
            listOf(
                listOf(false, true),
                listOf(false, true),
                listOf(true, true)
            ),
            listOf(
                listOf(true, true, true),
                listOf(true, false, false)
            ),
            listOf(
                listOf(true, true, true),
                listOf(false, false, true)
            ),

            // L-Shape 3x3 Large
            listOf(
                listOf(true, false, false),
                listOf(true, false, false),
                listOf(true, true, true)
            ),
            listOf(
                listOf(false, false, true),
                listOf(false, false, true),
                listOf(true, true, true)
            ),

            // T-Shapes
            listOf(
                listOf(true, true, true),
                listOf(false, true, false)
            ),
            listOf(
                listOf(false, true, false),
                listOf(true, true, true)
            ),
            listOf(
                listOf(true, false),
                listOf(true, true),
                listOf(true, false)
            ),
            listOf(
                listOf(false, true),
                listOf(true, true),
                listOf(false, true)
            ),

            // Z & S shapes
            listOf(
                listOf(true, true, false),
                listOf(false, true, true)
            ),
            listOf(
                listOf(false, true, true),
                listOf(true, true, false)
            ),

            // Plus (+) Cross
            listOf(
                listOf(false, true, false),
                listOf(true, true, true),
                listOf(false, true, false)
            )
        )
    }

    // ==========================================
    // 1. BOARD OCCUPANCY & PLACEMENT
    // ==========================================

    /**
     * Creates an empty board of [boardSize] x [boardSize] initialized with default [CellState].
     */
    fun createEmptyBoard(): List<List<CellState>> {
        return List(boardSize) {
            List(boardSize) { CellState() }
        }
    }

    /**
     * Validates whether a row and column coordinate is within the board boundary.
     */
    fun isWithinBounds(row: Int, col: Int): Boolean {
        return row in 0 until boardSize && col in 0 until boardSize
    }

    /**
     * Checks if a specific cell on the board is currently filled/occupied.
     */
    fun isCellOccupied(board: List<List<CellState>>, row: Int, col: Int): Boolean {
        if (!isWithinBounds(row, col)) return true
        return board[row][col].isFilled
    }

    /**
     * Checks if a piece can legally be placed at (startRow, startCol).
     */
    fun canPlacePiece(board: List<List<CellState>>, piece: BlockPiece, startRow: Int, startCol: Int): Boolean {
        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shapeMatrix[r][c]) {
                    val br = startRow + r
                    val bc = startCol + c
                    if (!isWithinBounds(br, bc)) return false
                    if (board[br][bc].isFilled) return false
                }
            }
        }
        return true
    }

    /**
     * Checks if a piece can fit anywhere on the entire board.
     */
    fun canPieceFitAnywhere(board: List<List<CellState>>, piece: BlockPiece): Boolean {
        val maxR = boardSize - piece.rows
        val maxC = boardSize - piece.cols
        if (maxR < 0 || maxC < 0) return false

        for (r in 0..maxR) {
            for (c in 0..maxC) {
                if (canPlacePiece(board, piece, r, c)) return true
            }
        }
        return false
    }

    /**
     * Checks whether at least one piece in the available pool can fit anywhere on the board.
     */
    fun canAnyPieceFit(board: List<List<CellState>>, pieces: List<BlockPiece?>): Boolean {
        return pieces.filterNotNull().any { canPieceFitAnywhere(board, it) }
    }

    /**
     * Returns the list of board coordinates that would be occupied by placing [piece] at (startRow, startCol).
     */
    fun getOccupiedCoordinates(piece: BlockPiece, startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val coords = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shapeMatrix[r][c]) {
                    coords.add(startRow + r to startCol + c)
                }
            }
        }
        return coords
    }

    /**
     * Calculates spatial occupancy metrics for the board.
     */
    fun calculateOccupancy(board: List<List<CellState>>): BoardOccupancy {
        var filledCount = 0
        val total = boardSize * boardSize
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                if (board[r][c].isFilled) filledCount++
            }
        }
        val ratio = filledCount.toFloat() / total
        return BoardOccupancy(
            filledCells = filledCount,
            totalCells = total,
            occupancyRatio = ratio,
            isDangerZone = ratio >= 0.75f
        )
    }

    /**
     * Immutably stamps a piece onto the board at (startRow, startCol).
     */
    fun stampPiece(
        board: List<List<CellState>>,
        piece: BlockPiece,
        startRow: Int,
        startCol: Int
    ): List<List<CellState>> {
        val newBoard = board.map { it.toMutableList() }
        for (r in 0 until piece.rows) {
            for (c in 0 until piece.cols) {
                if (piece.shapeMatrix[r][c]) {
                    val br = startRow + r
                    val bc = startCol + c
                    if (isWithinBounds(br, bc)) {
                        newBoard[br][bc] = CellState(
                            isFilled = true,
                            colorIndex = piece.colorIndex,
                            specialType = piece.specialType,
                            collectible = piece.collectible
                        )
                    }
                }
            }
        }
        return newBoard
    }

    // ==========================================
    // 2. PIECE GENERATION
    // ==========================================

    /**
     * Generates a single block piece with optional custom shape, color, or player level weighting.
     */
    fun generateSinglePiece(
        shape: List<List<Boolean>>? = null,
        colorIndex: Int? = null,
        special: SpecialType? = null,
        collectible: CollectibleType? = null,
        playerLevel: Int = 1
    ): BlockPiece {
        val chosenShape = shape ?: STANDARD_SHAPES.random()
        val chosenColor = colorIndex ?: (0..6).random()

        val chosenSpecial = special ?: if (playerLevel >= 3 && Math.random() < 0.08) {
            when ((0..4).random()) {
                0 -> SpecialType.BOMB
                1 -> SpecialType.LIGHTNING_H
                2 -> SpecialType.LIGHTNING_V
                3 -> SpecialType.RAINBOW
                else -> SpecialType.PRISM
            }
        } else SpecialType.NONE

        val chosenCollectible = collectible ?: if (Math.random() < 0.10) {
            when ((0..3).random()) {
                0 -> CollectibleType.STAR
                1 -> CollectibleType.GEM
                2 -> CollectibleType.COIN
                else -> CollectibleType.ENERGY_SHARD
            }
        } else CollectibleType.NONE

        return BlockPiece(
            id = UUID.randomUUID().toString(),
            shapeMatrix = chosenShape,
            colorIndex = chosenColor,
            specialType = chosenSpecial,
            collectible = chosenCollectible
        )
    }

    /**
     * Generates a trio of pieces with fairness verification ensuring at least one piece can fit.
     */
    fun generatePieceTrio(board: List<List<CellState>>, playerLevel: Int = 1): List<BlockPiece> {
        val trio = mutableListOf<BlockPiece>()
        for (i in 0 until 3) {
            trio.add(generateSinglePiece(playerLevel = playerLevel))
        }

        // Fairness check: Guarantee at least one piece can fit
        if (!canAnyPieceFit(board, trio)) {
            val smallCandidates = listOf(
                listOf(listOf(true)),
                listOf(listOf(true, true)),
                listOf(listOf(true), listOf(true)),
                listOf(listOf(true, true, true))
            )
            for (shape in smallCandidates) {
                val emergencyPiece = generateSinglePiece(shape = shape, playerLevel = playerLevel)
                if (canPieceFitAnywhere(board, emergencyPiece)) {
                    trio[0] = emergencyPiece
                    break
                }
            }
        }

        return trio
    }

    // ==========================================
    // 3. ROW & COLUMN CLEARING
    // ==========================================

    /**
     * Detects all full rows (0 until [boardSize]) on the board.
     */
    fun detectFullRows(board: List<List<CellState>>): List<Int> {
        return (0 until boardSize).filter { r ->
            board[r].all { it.isFilled }
        }
    }

    /**
     * Detects all full columns (0 until [boardSize]) on the board.
     */
    fun detectFullCols(board: List<List<CellState>>): List<Int> {
        return (0 until boardSize).filter { c ->
            (0 until boardSize).all { r -> board[r][c].isFilled }
        }
    }

    /**
     * Processes full rows and columns, triggers special blocks (bombs, lightnings, rainbows),
     * cracks ice obstacles, gathers collectibles, and returns the updated board state and results.
     */
    fun processLineClears(
        board: List<List<CellState>>,
        fullRows: List<Int>,
        fullCols: List<Int>
    ): ClearResult {
        val totalLines = fullRows.size + fullCols.size
        if (totalLines == 0) {
            return ClearResult(
                updatedBoard = board,
                fullRows = emptyList(),
                fullCols = emptyList(),
                totalLinesCleared = 0,
                clearedCells = emptySet(),
                specialEffectsTriggered = emptyList(),
                collectiblesGathered = emptyList()
            )
        }

        val boardState = board.map { it.toMutableList() }
        val primaryClears = mutableSetOf<Pair<Int, Int>>()

        for (r in fullRows) {
            for (c in 0 until boardSize) primaryClears.add(r to c)
        }
        for (c in fullCols) {
            for (r in 0 until boardSize) primaryClears.add(r to c)
        }

        val specialEffects = mutableListOf<SpecialEffectEvent>()
        val collectibles = mutableListOf<CollectibleEvent>()
        val secondaryClears = mutableSetOf<Pair<Int, Int>>()

        // Resolve special blocks and collectibles within cleared lines
        for ((r, c) in primaryClears) {
            val cell = boardState[r][c]

            if (cell.collectible != CollectibleType.NONE) {
                collectibles.add(CollectibleEvent(cell.collectible, r, c))
            }

            when (cell.specialType) {
                SpecialType.BOMB -> {
                    val blastCells = mutableSetOf<Pair<Int, Int>>()
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (isWithinBounds(nr, nc)) blastCells.add(nr to nc)
                        }
                    }
                    secondaryClears.addAll(blastCells)
                    specialEffects.add(SpecialEffectEvent(SpecialType.BOMB, r, c, blastCells))
                }
                SpecialType.LIGHTNING_H -> {
                    val rowBlast = (0 until boardSize).map { r to it }.toSet()
                    secondaryClears.addAll(rowBlast)
                    specialEffects.add(SpecialEffectEvent(SpecialType.LIGHTNING_H, r, c, rowBlast))
                }
                SpecialType.LIGHTNING_V -> {
                    val colBlast = (0 until boardSize).map { it to c }.toSet()
                    secondaryClears.addAll(colBlast)
                    specialEffects.add(SpecialEffectEvent(SpecialType.LIGHTNING_V, r, c, colBlast))
                }
                SpecialType.RAINBOW -> {
                    val adjacentBlast = mutableSetOf<Pair<Int, Int>>()
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (isWithinBounds(nr, nc)) adjacentBlast.add(nr to nc)
                        }
                    }
                    secondaryClears.addAll(adjacentBlast)
                    specialEffects.add(SpecialEffectEvent(SpecialType.RAINBOW, r, c, adjacentBlast))
                }
                else -> {}
            }
        }

        val allClears = primaryClears + secondaryClears

        // Fracture / Shatter nearby Ice Blocks
        for (r in 0 until boardSize) {
            for (c in 0 until boardSize) {
                val cell = boardState[r][c]
                if (cell.specialType == SpecialType.ICE && cell.isFilled) {
                    val inClearedLine = r in fullRows || c in fullCols
                    val isNearBlast = secondaryClears.any { (sr, sc) ->
                        kotlin.math.abs(sr - r) <= 1 && kotlin.math.abs(sc - c) <= 1
                    }
                    if (inClearedLine || isNearBlast) {
                        val hits = cell.iceHitsLeft - 1
                        if (hits <= 0) {
                            boardState[r][c] = CellState()
                        } else {
                            boardState[r][c] = cell.copy(iceHitsLeft = hits)
                        }
                    }
                }
            }
        }

        // Apply cleared cells to board
        for ((r, c) in allClears) {
            boardState[r][c] = CellState()
        }

        return ClearResult(
            updatedBoard = boardState,
            fullRows = fullRows,
            fullCols = fullCols,
            totalLinesCleared = totalLines,
            clearedCells = allClears,
            specialEffectsTriggered = specialEffects,
            collectiblesGathered = collectibles
        )
    }

    /**
     * Convenience method to detect and process any lines that can be cleared.
     */
    fun detectAndClear(board: List<List<CellState>>): ClearResult {
        val rows = detectFullRows(board)
        val cols = detectFullCols(board)
        return processLineClears(board, rows, cols)
    }

    // ==========================================
    // 4. COMBO MULTIPLIERS & SCORING
    // ==========================================

    /**
     * Calculates the combo multiplier based on streak count and momentum.
     */
    fun calculateComboMultiplier(combo: Int, momentumMultiplier: Float = 1.0f): Float {
        val streakMultiplier = when {
            combo <= 1 -> 1.0f
            combo == 2 -> 1.5f
            combo == 3 -> 2.0f
            combo == 4 -> 2.5f
            combo == 5 -> 3.0f
            else -> 1.0f + (combo * 0.5f)
        }
        return streakMultiplier * momentumMultiplier
    }

    /**
     * Calculates points earned from placing a piece on the board.
     */
    fun calculatePlacementScore(piece: BlockPiece, momentumMultiplier: Float = 1.0f): Int {
        return (piece.blockCount * 10 * momentumMultiplier).toInt()
    }

    /**
     * Computes the line clear score breakdown including multi-line base score,
     * combo bonus, and momentum scaling.
     */
    fun calculateLineClearScore(
        linesCleared: Int,
        combo: Int,
        momentumMultiplier: Float = 1.0f
    ): ScoreBreakdown {
        val lineBase = when (linesCleared) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            5 -> 1500
            else -> 2200
        }
        val comboBonus = (combo.coerceAtLeast(1) - 1) * 150
        val multiplier = calculateComboMultiplier(combo, momentumMultiplier)
        val total = ((lineBase + comboBonus) * momentumMultiplier).toInt()
        val banner = getComboBannerText(combo, linesCleared)

        return ScoreBreakdown(
            baseLineScore = lineBase,
            comboBonus = comboBonus,
            comboMultiplier = multiplier,
            totalScoreEarned = total,
            bannerText = banner
        )
    }

    /**
     * Generates energetic combo banner text.
     */
    fun getComboBannerText(combo: Int, linesCleared: Int): String {
        return when (combo) {
            1 -> if (linesCleared > 1) "${linesCleared}X LINE CLEAR!" else "LINE CLEAR!"
            2 -> "2X FLOW!"
            3 -> "PERFECT COMBO!"
            4 -> "INSANE FLOW!"
            5 -> "ULTRA COMBO!"
            else -> "BLOCK GOD ${combo}X!"
        }
    }
}

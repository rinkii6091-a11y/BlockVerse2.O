package com.example.model

enum class SpecialType {
    NONE,
    BOMB,
    LIGHTNING_H,
    LIGHTNING_V,
    RAINBOW,
    PRISM,
    ICE
}

enum class CollectibleType {
    NONE,
    STAR,
    GEM,
    COIN,
    ENERGY_SHARD
}

enum class PowerUpType {
    HAMMER,
    SHUFFLE,
    UNDO,
    BOMB,
    COLOR_BLAST
}

data class Point(val r: Int, val c: Int)

data class BlockPiece(
    val id: String,
    val shapeMatrix: List<List<Boolean>>,
    val colorIndex: Int,
    val specialType: SpecialType = SpecialType.NONE,
    val collectible: CollectibleType = CollectibleType.NONE
) {
    val rows: Int get() = shapeMatrix.size
    val cols: Int get() = if (shapeMatrix.isNotEmpty()) shapeMatrix[0].size else 0

    val blockCount: Int get() = shapeMatrix.sumOf { row -> row.count { it } }

    /**
     * Returns true if this piece can fit at (startRow, startCol) on an 8x8 board.
     */
    fun canPlaceAt(board: List<List<CellState>>, startRow: Int, startCol: Int): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (shapeMatrix[r][c]) {
                    val boardR = startRow + r
                    val boardC = startCol + c
                    if (boardR !in 0..7 || boardC !in 0..7) return false
                    if (board[boardR][boardC].isFilled) return false
                }
            }
        }
        return true
    }

    /**
     * Checks if this piece can be placed anywhere on the 8x8 board.
     */
    fun canFitAnywhere(board: List<List<CellState>>): Boolean {
        for (r in 0..(8 - rows)) {
            for (c in 0..(8 - cols)) {
                if (canPlaceAt(board, r, c)) return true
            }
        }
        return false
    }
}

data class CellState(
    val isFilled: Boolean = false,
    val colorIndex: Int = 0,
    val specialType: SpecialType = SpecialType.NONE,
    val collectible: CollectibleType = CollectibleType.NONE,
    val isClearing: Boolean = false,
    val isGhostPreview: Boolean = false,
    val isInvalidShake: Boolean = false,
    val iceHitsLeft: Int = 0 // For ice blocks
)

object PieceTemplates {
    private val SHAPES = listOf(
        // 1x1
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

        // L-Shape 3x3
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

        // T-Shape
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

        // Plus (+)
        listOf(
            listOf(false, true, false),
            listOf(true, true, true),
            listOf(false, true, false)
        )
    )

    /**
     * Smart piece generator.
     * Guarantees that at least one of the 3 generated pieces can legally be placed on the current board,
     * preventing sudden unfair immediate game-over.
     */
    fun generateTrio(board: List<List<CellState>>, playerLevel: Int = 1): List<BlockPiece> {
        val result = mutableListOf<BlockPiece>()
        val colorPool = (0..6).toList()

        // 1. Generate candidate pieces
        for (i in 0 until 3) {
            val randomShape = SHAPES.random()
            val color = colorPool.random()

            // Special block chance: 8% on level 3+
            val special = if (playerLevel >= 3 && Math.random() < 0.08) {
                when ((0..4).random()) {
                    0 -> SpecialType.BOMB
                    1 -> SpecialType.LIGHTNING_H
                    2 -> SpecialType.LIGHTNING_V
                    3 -> SpecialType.RAINBOW
                    else -> SpecialType.PRISM
                }
            } else SpecialType.NONE

            // Collectible chance: 10%
            val collectible = if (Math.random() < 0.10) {
                when ((0..3).random()) {
                    0 -> CollectibleType.STAR
                    1 -> CollectibleType.GEM
                    2 -> CollectibleType.COIN
                    else -> CollectibleType.ENERGY_SHARD
                }
            } else CollectibleType.NONE

            result.add(
                BlockPiece(
                    id = java.util.UUID.randomUUID().toString(),
                    shapeMatrix = randomShape,
                    colorIndex = color,
                    specialType = special,
                    collectible = collectible
                )
            )
        }

        // 2. Ensure at least one piece can fit
        val hasFit = result.any { it.canFitAnywhere(board) }
        if (!hasFit) {
            // Find single dot or 2-block line that fits
            val smallerCandidates = listOf(
                listOf(listOf(true)),
                listOf(listOf(true, true)),
                listOf(listOf(true), listOf(true)),
                listOf(listOf(true, true, true))
            )
            for (shape in smallerCandidates) {
                val candidate = BlockPiece(
                    id = java.util.UUID.randomUUID().toString(),
                    shapeMatrix = shape,
                    colorIndex = (0..6).random()
                )
                if (candidate.canFitAnywhere(board)) {
                    result[0] = candidate
                    break
                }
            }
        }

        return result
    }
}

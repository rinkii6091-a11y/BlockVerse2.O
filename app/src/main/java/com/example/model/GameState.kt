package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.theme.ThemeId

enum class GameMode {
    CLASSIC,
    ADVENTURE,
    DAILY,
    TIME_RUSH
}

enum class LevelDifficulty(val label: String, val colorHex: Long) {
    EASY("EASY", 0xFF10B981),
    MEDIUM("MEDIUM", 0xFF00E5FF),
    HARD("HARD", 0xFFF59E0B),
    EXPERT("EXPERT", 0xFFFF007A),
    INSANE("INSANE", 0xFFA855F7)
}

enum class MomentumLevel(val label: String, val multiplier: Float, val colorHex: Long) {
    CALM("CALM", 1.0f, 0xFF38BDF8),
    FLOW("2X FLOW", 1.5f, 0xFF00E5FF),
    HYPE("HYPE 2X", 2.0f, 0xFFA855F7),
    OVERDRIVE("OVERDRIVE 3X!", 3.0f, 0xFFFF2E93)
}

/**
 * State container for the Game Score Header UI component.
 * Managed and emitted reactively by GameViewModel.
 */
data class ScoreHeaderState(
    val currentScore: Int = 0,
    val bestScore: Int = 0,
    val isNewRecord: Boolean = false,
    val combo: Int = 0,
    val multiplier: Float = 1.0f,
    val momentum: MomentumLevel = MomentumLevel.CALM,
    val gameMode: GameMode = GameMode.CLASSIC,
    val adventureLevel: Int = 1
)

data class FloatingScore(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val startX: Float,
    val startY: Float,
    val color: Color = Color(0xFFFFE066),
    val creationTime: Long = System.currentTimeMillis()
)

enum class ParticleType {
    NEON_CIRCLE,
    STAR_SPARKLE,
    NEON_RING,
    LIGHT_STREAK
}

data class Particle(
    val id: String = java.util.UUID.randomUUID().toString(),
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var alpha: Float = 1.0f,
    val maxLife: Float = 1.0f,
    var life: Float = 1.0f,
    val type: ParticleType = ParticleType.NEON_CIRCLE,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    val length: Float = 0f
)

data class AdventureLevel(
    val levelNumber: Int,
    val title: String,
    val description: String,
    val difficulty: LevelDifficulty,
    val targetScore: Int,
    val targetLines: Int,
    val targetCombos: Int = 0,
    val starRequirements: List<Int>, // 1, 2, 3 stars
    val hasObstacles: Boolean = false,
    val hasIceBlocks: Boolean = false,
    val obstaclePattern: String = "NONE" // NONE, CORNERS, CROSS, CENTER_ICE, RING, DENSE
)

object AdventureLevelsCatalog {
    val levels = listOf(
        // ================= TIER 1: EASY (Levels 1 - 5) =================
        AdventureLevel(
            levelNumber = 1,
            title = "First Steps",
            description = "Clear 3 lines on a clean board to calibrate grid flow",
            difficulty = LevelDifficulty.EASY,
            targetScore = 800,
            targetLines = 3,
            starRequirements = listOf(600, 1000, 1500)
        ),
        AdventureLevel(
            levelNumber = 2,
            title = "Grid Fundamentals",
            description = "Clear 5 lines and keep the grid spacious",
            difficulty = LevelDifficulty.EASY,
            targetScore = 1500,
            targetLines = 5,
            starRequirements = listOf(1200, 1800, 2500)
        ),
        AdventureLevel(
            levelNumber = 3,
            title = "Double Sync",
            description = "Execute a 2x Combo and clear 7 lines",
            difficulty = LevelDifficulty.EASY,
            targetScore = 2400,
            targetLines = 7,
            targetCombos = 2,
            starRequirements = listOf(2000, 2800, 3800)
        ),
        AdventureLevel(
            levelNumber = 4,
            title = "Color Spark",
            description = "Clear 8 lines and score 3,200 points",
            difficulty = LevelDifficulty.EASY,
            targetScore = 3200,
            targetLines = 8,
            starRequirements = listOf(2600, 3600, 4600)
        ),
        AdventureLevel(
            levelNumber = 5,
            title = "Rookie Mastery",
            description = "Reach a 2x Combo and clear 10 lines",
            difficulty = LevelDifficulty.EASY,
            targetScore = 4200,
            targetLines = 10,
            targetCombos = 2,
            starRequirements = listOf(3500, 4800, 6000)
        ),

        // ================= TIER 2: MEDIUM (Levels 6 - 10) =================
        AdventureLevel(
            levelNumber = 6,
            title = "Corner Hazard",
            description = "Obstacles detected in grid corners! Clear 10 lines",
            difficulty = LevelDifficulty.MEDIUM,
            targetScore = 5200,
            targetLines = 10,
            hasObstacles = true,
            obstaclePattern = "CORNERS",
            starRequirements = listOf(4200, 5800, 7500)
        ),
        AdventureLevel(
            levelNumber = 7,
            title = "Bomb Calibration",
            description = "Special bombs are active! Explode 3x3 zones and clear 12 lines",
            difficulty = LevelDifficulty.MEDIUM,
            targetScore = 6500,
            targetLines = 12,
            starRequirements = listOf(5500, 7200, 9200)
        ),
        AdventureLevel(
            levelNumber = 8,
            title = "Flow Resonance",
            description = "Chain consecutive clears to reach a 3x Combo",
            difficulty = LevelDifficulty.MEDIUM,
            targetScore = 7800,
            targetLines = 14,
            targetCombos = 3,
            starRequirements = listOf(6800, 8800, 11000)
        ),
        AdventureLevel(
            levelNumber = 9,
            title = "Quadrant Lock",
            description = "Four obstacle nodes blocking corridors. Clear 15 lines",
            difficulty = LevelDifficulty.MEDIUM,
            targetScore = 9000,
            targetLines = 15,
            hasObstacles = true,
            obstaclePattern = "QUADRANTS",
            starRequirements = listOf(8000, 10200, 13000)
        ),
        AdventureLevel(
            levelNumber = 10,
            title = "Apprentice Graduation",
            description = "Reach 3x Combo and score 10,500 points",
            difficulty = LevelDifficulty.MEDIUM,
            targetScore = 10500,
            targetLines = 16,
            targetCombos = 3,
            starRequirements = listOf(9500, 12000, 15000)
        ),

        // ================= TIER 3: HARD (Levels 11 - 15) =================
        AdventureLevel(
            levelNumber = 11,
            title = "Cryo Genesis",
            description = "Frozen ice blocks in center! Clear adjacent lines to shatter them",
            difficulty = LevelDifficulty.HARD,
            targetScore = 12000,
            targetLines = 16,
            hasIceBlocks = true,
            obstaclePattern = "CENTER_ICE",
            starRequirements = listOf(10500, 13500, 17000)
        ),
        AdventureLevel(
            levelNumber = 12,
            title = "Frost & Detonation",
            description = "Ice blocks + active bombs. Clear 18 lines",
            difficulty = LevelDifficulty.HARD,
            targetScore = 14000,
            targetLines = 18,
            hasIceBlocks = true,
            obstaclePattern = "CENTER_ICE",
            starRequirements = listOf(12500, 15800, 19500)
        ),
        AdventureLevel(
            levelNumber = 13,
            title = "Hype Pressure",
            description = "Unleash a 4x Combo streak! Clear 20 lines",
            difficulty = LevelDifficulty.HARD,
            targetScore = 16500,
            targetLines = 20,
            targetCombos = 4,
            starRequirements = listOf(14500, 18500, 23000)
        ),
        AdventureLevel(
            levelNumber = 14,
            title = "Glacier Cross",
            description = "Cross-shaped ice formations. Shatter all sectors",
            difficulty = LevelDifficulty.HARD,
            targetScore = 19000,
            targetLines = 22,
            hasIceBlocks = true,
            obstaclePattern = "CROSS_ICE",
            starRequirements = listOf(17000, 21500, 26500)
        ),
        AdventureLevel(
            levelNumber = 15,
            title = "Challenger Summit",
            description = "Reach 4x Combo, clear 24 lines, score 22,000 pts",
            difficulty = LevelDifficulty.HARD,
            targetScore = 22000,
            targetLines = 24,
            targetCombos = 4,
            hasObstacles = true,
            obstaclePattern = "CORNERS",
            starRequirements = listOf(19500, 24500, 30500)
        ),

        // ================= TIER 4: EXPERT (Levels 16 - 20) =================
        AdventureLevel(
            levelNumber = 16,
            title = "Prism Synthesis",
            description = "Prism chain reactions with 4 ice blocks. Clear 26 lines",
            difficulty = LevelDifficulty.EXPERT,
            targetScore = 25000,
            targetLines = 26,
            hasIceBlocks = true,
            obstaclePattern = "CENTER_ICE",
            starRequirements = listOf(22000, 28000, 35000)
        ),
        AdventureLevel(
            levelNumber = 17,
            title = "Perimeter Constriction",
            description = "Perimeter barriers reduce placement room. Clear 28 lines",
            difficulty = LevelDifficulty.EXPERT,
            targetScore = 28500,
            targetLines = 28,
            hasObstacles = true,
            obstaclePattern = "PERIMETER",
            starRequirements = listOf(25000, 32000, 40000)
        ),
        AdventureLevel(
            levelNumber = 18,
            title = "Overdrive Velocity",
            description = "Hit a 5x Combo streak! High velocity scoring",
            difficulty = LevelDifficulty.EXPERT,
            targetScore = 32000,
            targetLines = 30,
            targetCombos = 5,
            starRequirements = listOf(28000, 36000, 45000)
        ),
        AdventureLevel(
            levelNumber = 19,
            title = "Cryo Matrix",
            description = "8 staggered ice blocks across the grid. Clear 32 lines",
            difficulty = LevelDifficulty.EXPERT,
            targetScore = 36000,
            targetLines = 32,
            hasIceBlocks = true,
            obstaclePattern = "STAGGERED_ICE",
            starRequirements = listOf(32000, 41000, 51000)
        ),
        AdventureLevel(
            levelNumber = 20,
            title = "Grandmaster Proving",
            description = "5x Combo, 35 lines, score 40,000 points",
            difficulty = LevelDifficulty.EXPERT,
            targetScore = 40000,
            targetLines = 35,
            targetCombos = 5,
            hasObstacles = true,
            obstaclePattern = "QUADRANTS",
            starRequirements = listOf(36000, 46000, 58000)
        ),

        // ================= TIER 5: INSANE (Levels 21 - 25) =================
        AdventureLevel(
            levelNumber = 21,
            title = "Supernova Grid",
            description = "Diamond obstacle core + ice pillars. Clear 36 lines",
            difficulty = LevelDifficulty.INSANE,
            targetScore = 45000,
            targetLines = 36,
            hasObstacles = true,
            hasIceBlocks = true,
            obstaclePattern = "DIAMOND_ICE",
            starRequirements = listOf(40000, 52000, 65000)
        ),
        AdventureLevel(
            levelNumber = 22,
            title = "Quantum Resonance",
            description = "Reach an incredible 6x Combo! Clear 38 lines",
            difficulty = LevelDifficulty.INSANE,
            targetScore = 50000,
            targetLines = 38,
            targetCombos = 6,
            starRequirements = listOf(45000, 58000, 72000)
        ),
        AdventureLevel(
            levelNumber = 23,
            title = "Event Horizon",
            description = "Dense labyrinth corridors. Master your placements",
            difficulty = LevelDifficulty.INSANE,
            targetScore = 55000,
            targetLines = 40,
            hasObstacles = true,
            obstaclePattern = "DENSE",
            starRequirements = listOf(50000, 64000, 80000)
        ),
        AdventureLevel(
            levelNumber = 24,
            title = "Void Apocalypse",
            description = "Double ring ice fields. Clear 42 lines and shatter all",
            difficulty = LevelDifficulty.INSANE,
            targetScore = 60000,
            targetLines = 42,
            hasIceBlocks = true,
            obstaclePattern = "RING_ICE",
            starRequirements = listOf(55000, 70000, 88000)
        ),
        AdventureLevel(
            levelNumber = 25,
            title = "Block God Overdrive",
            description = "The ultimate block puzzle pinnacle: 6x Combo, 45 lines, 68,000 pts",
            difficulty = LevelDifficulty.INSANE,
            targetScore = 68000,
            targetLines = 45,
            targetCombos = 6,
            hasObstacles = true,
            hasIceBlocks = true,
            obstaclePattern = "DIAMOND_ICE",
            starRequirements = listOf(62000, 78000, 98000)
        )
    )

    fun getLevel(num: Int): AdventureLevel = levels.firstOrNull { it.levelNumber == num } ?: levels.last()

    fun getLevelsByDifficulty(diff: LevelDifficulty): List<AdventureLevel> {
        return levels.filter { it.difficulty == diff }
    }
}

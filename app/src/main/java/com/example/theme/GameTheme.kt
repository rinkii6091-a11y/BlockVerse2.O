package com.example.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Visual Theme definitions for BlockFlow.
 * Includes colors for blocks, backgrounds, highlights, grid lines, and particle effects.
 */
enum class ThemeId {
    NEON_CITY,
    CYBER_SPACE,
    CANDY_DREAM,
    HOLOGRAPHIC,
    VOLCANIC,
    RETRO_ARCADE,
    OCEAN_ABYSS,
    AURORA_BOREALIS
}

data class BlockColorData(
    val mainColor: Color,
    val topHighlight: Color,
    val bottomShadow: Color,
    val glowColor: Color
)

data class ThemeConfig(
    val id: ThemeId,
    val name: String,
    val description: String,
    val costCoins: Int,
    val costGems: Int,
    val backgroundGradient: Brush,
    val boardBackground: Color,
    val boardBorder: Color,
    val cellEmptyBackground: Color,
    val cellEmptyBorder: Color,
    val accentColor: Color,
    val secondaryAccent: Color,
    val particleColors: List<Color>,
    val blockPalette: List<BlockColorData>
)

object GameThemes {
    val NeonCity = ThemeConfig(
        id = ThemeId.NEON_CITY,
        name = "Neon City",
        description = "High-voltage 2026 cyberpunk neon aesthetics",
        costCoins = 0,
        costGems = 0,
        backgroundGradient = Brush.verticalGradient(
            listOf(Color(0xFF0A0E1A), Color(0xFF131127), Color(0xFF080914))
        ),
        boardBackground = Color(0xDD0F172A),
        boardBorder = Color(0x6600F0FF),
        cellEmptyBackground = Color(0x331E293B),
        cellEmptyBorder = Color(0x2238BDF8),
        accentColor = Color(0xFF00F0FF),
        secondaryAccent = Color(0xFFFF007A),
        particleColors = listOf(
            Color(0xFF00F0FF),
            Color(0xFFFF007A),
            Color(0xFFA855F7),
            Color(0xFFFBBF24),
            Color(0xFF38BDF8)
        ),
        blockPalette = listOf(
            BlockColorData(Color(0xFF00E5FF), Color(0xFFE0F7FA), Color(0xFF00838F), Color(0x8800E5FF)), // Cyan
            BlockColorData(Color(0xFFFF2E93), Color(0xFFFCE4EC), Color(0xFFAD1457), Color(0x88FF2E93)), // Magenta Pink
            BlockColorData(Color(0xFFA855F7), Color(0xFFF3E8FF), Color(0xFF6B21A8), Color(0x88A855F7)), // Violet
            BlockColorData(Color(0xFFF59E0B), Color(0xFFFFFBEB), Color(0xFFB45309), Color(0x88F59E0B)), // Amber Gold
            BlockColorData(Color(0xFF10B981), Color(0xFFECFDF5), Color(0xFF047857), Color(0x8810B981)), // Emerald
            BlockColorData(Color(0xFF3B82F6), Color(0xFFEFF6FF), Color(0xFF1D4ED8), Color(0x883B82F6)), // Electric Blue
            BlockColorData(Color(0xFFEF4444), Color(0xFFFEF2F2), Color(0xFFB91C1C), Color(0x88EF4444))  // Crimson
        )
    )

    val CyberSpace = ThemeConfig(
        id = ThemeId.CYBER_SPACE,
        name = "Cyber Space",
        description = "Deep cosmos with orbital starfields & pulsar blues",
        costCoins = 800,
        costGems = 10,
        backgroundGradient = Brush.verticalGradient(
            listOf(Color(0xFF050814), Color(0xFF0B122B), Color(0xFF03050D))
        ),
        boardBackground = Color(0xDD091024),
        boardBorder = Color(0x666366F1),
        cellEmptyBackground = Color(0x261E1B4B),
        cellEmptyBorder = Color(0x22818CF8),
        accentColor = Color(0xFF6366F1),
        secondaryAccent = Color(0xFF38BDF8),
        particleColors = listOf(Color(0xFF818CF8), Color(0xFF38BDF8), Color(0xFFC084FC), Color(0xFFFFFFFF)),
        blockPalette = listOf(
            BlockColorData(Color(0xFF6366F1), Color(0xFFE0E7FF), Color(0xFF3730A3), Color(0x886366F1)),
            BlockColorData(Color(0xFF06B6D4), Color(0xFFCFFAFE), Color(0xFF0E7490), Color(0x8806B6D4)),
            BlockColorData(Color(0xFF8B5CF6), Color(0xFFEDE9FE), Color(0xFF5B21B6), Color(0x888B5CF6)),
            BlockColorData(Color(0xFF38BDF8), Color(0xFFF0F9FF), Color(0xFF0369A1), Color(0x8838BDF8)),
            BlockColorData(Color(0xFFE0E7FF), Color(0xFFFFFFFF), Color(0xFF6366F1), Color(0x88E0E7FF)),
            BlockColorData(Color(0xFF4F46E5), Color(0xFFEEF2FF), Color(0xFF312E81), Color(0x884F46E5)),
            BlockColorData(Color(0xFF9333EA), Color(0xFFFAF5FF), Color(0xFF681A9E), Color(0x889333EA))
        )
    )

    val CandyDream = ThemeConfig(
        id = ThemeId.CANDY_DREAM,
        name = "Candy Dream",
        description = "Sweet pastel confectionery wonderland",
        costCoins = 1200,
        costGems = 15,
        backgroundGradient = Brush.verticalGradient(
            listOf(Color(0xFF24142B), Color(0xFF2F1735), Color(0xFF1B0F21))
        ),
        boardBackground = Color(0xDD2D1A38),
        boardBorder = Color(0x66F472B6),
        cellEmptyBackground = Color(0x334A2859),
        cellEmptyBorder = Color(0x22F472B6),
        accentColor = Color(0xFFF472B6),
        secondaryAccent = Color(0xFF34D399),
        particleColors = listOf(Color(0xFFF472B6), Color(0xFFFDE047), Color(0xFF6EE7B7), Color(0xFF93C5FD)),
        blockPalette = listOf(
            BlockColorData(Color(0xFFF472B6), Color(0xFFFDF2F8), Color(0xFFBE185D), Color(0x88F472B6)),
            BlockColorData(Color(0xFF34D399), Color(0xFFECFDF5), Color(0xFF059669), Color(0x8834D399)),
            BlockColorData(Color(0xFFFBBF24), Color(0xFFFEF3C7), Color(0xFFD97706), Color(0x88FBBF24)),
            BlockColorData(Color(0xFF60A5FA), Color(0xFFEFF6FF), Color(0xFF2563EB), Color(0x8860A5FA)),
            BlockColorData(Color(0xFFA78BFA), Color(0xFFF5F3FF), Color(0xFF7C3AED), Color(0x88A78BFA)),
            BlockColorData(Color(0xFFFB7185), Color(0xFFFFF1F2), Color(0xFFE11D48), Color(0x88FB7185)),
            BlockColorData(Color(0xFF38BDF8), Color(0xFFF0F9FF), Color(0xFF0284C7), Color(0x8838BDF8))
        )
    )

    val Volcanic = ThemeConfig(
        id = ThemeId.VOLCANIC,
        name = "Volcanic Core",
        description = "Molten magma, obsidian shards and heat embers",
        costCoins = 1800,
        costGems = 25,
        backgroundGradient = Brush.verticalGradient(
            listOf(Color(0xFF140808), Color(0xFF260D0A), Color(0xFF0F0404))
        ),
        boardBackground = Color(0xDD200B09),
        boardBorder = Color(0x66F97316),
        cellEmptyBackground = Color(0x333F1712),
        cellEmptyBorder = Color(0x22FB923C),
        accentColor = Color(0xFFFF5722),
        secondaryAccent = Color(0xFFFBBF24),
        particleColors = listOf(Color(0xFFFF5722), Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFDC2626)),
        blockPalette = listOf(
            BlockColorData(Color(0xFFFF5722), Color(0xFFFFCCBC), Color(0xFFBF360C), Color(0x88FF5722)),
            BlockColorData(Color(0xFFF59E0B), Color(0xFFFEF3C7), Color(0xFFB45309), Color(0x88F59E0B)),
            BlockColorData(Color(0xFFEF4444), Color(0xFFFEE2E2), Color(0xFF991B1B), Color(0x88EF4444)),
            BlockColorData(Color(0xFFFB923C), Color(0xFFFFEDD5), Color(0xFFC2410C), Color(0x88FB923C)),
            BlockColorData(Color(0xFFFACC15), Color(0xFFFEF9C3), Color(0xFFA16207), Color(0x88FACC15)),
            BlockColorData(Color(0xFFDC2626), Color(0xFFFEE2E2), Color(0xFF7F1D1D), Color(0x88DC2626)),
            BlockColorData(Color(0xFFEA580C), Color(0xFFFFEDD5), Color(0xFF9A3412), Color(0x88EA580C))
        )
    )

    val Holographic = ThemeConfig(
        id = ThemeId.HOLOGRAPHIC,
        name = "Holographic",
        description = "Prismatic iridescence with luminous highlights",
        costCoins = 2500,
        costGems = 35,
        backgroundGradient = Brush.verticalGradient(
            listOf(Color(0xFF0F1523), Color(0xFF161E33), Color(0xFF0C101B))
        ),
        boardBackground = Color(0xDD111B2E),
        boardBorder = Color(0x66A5B4FC),
        cellEmptyBackground = Color(0x331E2D4A),
        cellEmptyBorder = Color(0x22818CF8),
        accentColor = Color(0xFFC084FC),
        secondaryAccent = Color(0xFF38BDF8),
        particleColors = listOf(Color(0xFFE0E7FF), Color(0xFFC084FC), Color(0xFF67E8F9), Color(0xFFF472B6)),
        blockPalette = listOf(
            BlockColorData(Color(0xFF818CF8), Color(0xFFEEF2FF), Color(0xFF4338CA), Color(0x88818CF8)),
            BlockColorData(Color(0xFFC084FC), Color(0xFFFAF5FF), Color(0xFF7E22CE), Color(0x88C084FC)),
            BlockColorData(Color(0xFF38BDF8), Color(0xFFF0F9FF), Color(0xFF0369A1), Color(0x8838BDF8)),
            BlockColorData(Color(0xFF34D399), Color(0xFFECFDF5), Color(0xFF047857), Color(0x8834D399)),
            BlockColorData(Color(0xFFF472B6), Color(0xFFFDF2F8), Color(0xFFBE185D), Color(0x88F472B6)),
            BlockColorData(Color(0xFFFBBF24), Color(0xFFFFFBEB), Color(0xFFB45309), Color(0x88FBBF24)),
            BlockColorData(Color(0xFFA5B4FC), Color(0xFFFFFFFF), Color(0xFF4F46E5), Color(0x88A5B4FC))
        )
    )

    val allThemes: List<ThemeConfig> = listOf(
        NeonCity,
        CyberSpace,
        CandyDream,
        Volcanic,
        Holographic
    )

    fun getById(id: ThemeId): ThemeConfig = allThemes.firstOrNull { it.id == id } ?: NeonCity
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val highScore: Int = 0,
    val currentLevel: Int = 1,
    val levelStars: Int = 0,
    val coins: Int = 250,
    val gems: Int = 15,
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val bestCombo: Int = 0,
    val totalBlocksPlaced: Int = 0,
    val dailyStreak: Int = 1,
    val lastStreakDate: String = "",
    val activeThemeId: String = "NEON_CITY",
    val unlockedThemes: String = "NEON_CITY",
    val hammerCount: Int = 3,
    val shuffleCount: Int = 3,
    val undoCount: Int = 3,
    val bombCount: Int = 2,
    val colorBlastCount: Int = 2,
    val isTutorialCompleted: Boolean = false,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val colorblindMode: Boolean = false
)

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0,
    val highScore: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val dateKey: String, // e.g., "2026-09-28"
    val description: String,
    val targetType: String, // "COMBO", "LINES", "SCORE"
    val targetValue: Int,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val isUnlocked: Boolean,
    val rewardCoins: Int,
    val rewardGems: Int
)

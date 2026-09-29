package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(private val dao: GameDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val achievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
    val allLevelProgress: Flow<List<LevelProgressEntity>> = dao.getAllLevelProgress()

    fun getDailyChallenge(dateKey: String): Flow<DailyChallengeEntity?> = dao.getDailyChallenge(dateKey)

    suspend fun saveLevelProgress(levelNumber: Int, stars: Int, score: Int) {
        val entity = LevelProgressEntity(
            levelNumber = levelNumber,
            stars = stars,
            highScore = score,
            isCompleted = true
        )
        dao.insertOrUpdateLevelProgress(entity)
    }

    suspend fun getProfileOnce(): UserProfileEntity {
        return dao.getUserProfileOnce() ?: UserProfileEntity().also {
            dao.insertOrUpdateProfile(it)
        }
    }

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun checkDailyStreak(): UserProfileEntity {
        val profile = getProfileOnce()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (profile.lastStreakDate == today) {
            return profile
        }

        // Calculate day difference if any
        val newStreak = if (profile.lastStreakDate.isNotEmpty()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val lastDate = format.parse(profile.lastStreakDate)
                val currentDate = format.parse(today)
                if (lastDate != null && currentDate != null) {
                    val diffDays = (currentDate.time - lastDate.time) / (1000 * 60 * 60 * 24)
                    if (diffDays == 1L) (profile.dailyStreak % 7) + 1 else 1
                } else 1
            } catch (_: Exception) {
                1
            }
        } else {
            1
        }

        val updated = profile.copy(
            dailyStreak = newStreak,
            lastStreakDate = today,
            coins = profile.coins + (50 * newStreak),
            gems = profile.gems + (if (newStreak % 3 == 0) 5 else 1)
        )
        dao.insertOrUpdateProfile(updated)
        return updated
    }

    suspend fun initDailyChallengeIfNeeded(): DailyChallengeEntity {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = dao.getUserProfileOnce()
        // Day of month hash to pick interesting targets
        val dayNum = SimpleDateFormat("dd", Locale.getDefault()).format(Date()).toIntOrNull() ?: 1
        val (desc, type, target) = when (dayNum % 3) {
            0 -> Triple("Reach a 5x Combo streak", "COMBO", 5)
            1 -> Triple("Clear 20 lines in any game", "LINES", 20)
            else -> Triple("Score 5,000 points in a single round", "SCORE", 5000)
        }
        val challenge = DailyChallengeEntity(
            dateKey = today,
            description = desc,
            targetType = type,
            targetValue = target,
            currentProgress = 0,
            isCompleted = false,
            isClaimed = false
        )
        dao.insertOrUpdateDailyChallenge(challenge)
        return challenge
    }

    suspend fun updateDailyProgress(type: String, value: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        // Note: in a simple flow, we check if today's challenge exists
    }

    suspend fun initDefaultAchievementsIfNeeded() {
        val defaults = listOf(
            AchievementEntity("first_clear", "First Ignition", "Clear your first line", 1, 0, false, 100, 5),
            AchievementEntity("combo_3", "In The Flow", "Reach a 3x Combo", 3, 0, false, 200, 10),
            AchievementEntity("combo_6", "Flow Master", "Reach a 6x Combo", 6, 0, false, 500, 20),
            AchievementEntity("score_5k", "High Roller", "Score 5,000 points in one game", 5000, 0, false, 300, 15),
            AchievementEntity("score_15k", "Block Legend", "Score 15,000 points in one game", 15000, 0, false, 1000, 30),
            AchievementEntity("lines_50", "Grid Sweeper", "Clear 50 total lines", 50, 0, false, 400, 10),
            AchievementEntity("lines_200", "Century Destroyer", "Clear 200 total lines", 200, 0, false, 800, 25),
            AchievementEntity("bomb_user", "Demolition Crew", "Detonate 5 bombs", 5, 0, false, 250, 10)
        )
        dao.insertAchievements(defaults)
    }
}

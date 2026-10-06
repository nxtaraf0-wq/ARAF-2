package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("araf_game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COINS = "araf_coins"
        private const val KEY_COMPLETED_LEVELS = "araf_completed_levels"
        private const val KEY_SOUND_ENABLED = "araf_sound_enabled"
        private const val KEY_TOTAL_WORDS_FOUND = "araf_total_words"
        private const val KEY_GAMES_PLAYED = "araf_games_played"
        private const val KEY_HIGH_SCORE = "araf_high_score"
    }

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 300)
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    fun addCoins(amount: Int) {
        coins += amount
    }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    var completedLevels: Set<String>
        get() = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, value).apply()

    fun markLevelCompleted(categoryId: String) {
        val current = completedLevels.toMutableSet()
        current.add(categoryId)
        completedLevels = current
    }

    fun isLevelUnlocked(categoryIndex: Int, categories: List<com.example.model.Category>): Boolean {
        // Level 0 (Animals) is always unlocked
        if (categoryIndex == 0) return true
        val prevCategoryId = categories.getOrNull(categoryIndex - 1)?.id ?: return false
        return completedLevels.contains(prevCategoryId)
    }

    var totalWordsFound: Int
        get() = prefs.getInt(KEY_TOTAL_WORDS_FOUND, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_WORDS_FOUND, value).apply()

    fun incrementWordsFound(count: Int = 1) {
        totalWordsFound += count
    }

    var gamesPlayed: Int
        get() = prefs.getInt(KEY_GAMES_PLAYED, 0)
        set(value) = prefs.edit().putInt(KEY_GAMES_PLAYED, value).apply()

    fun incrementGamesPlayed() {
        gamesPlayed += 1
    }

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()

    fun updateHighScore(score: Int) {
        if (score > highScore) {
            highScore = score
        }
    }

    fun getBestTime(categoryId: String): Int? {
        val time = prefs.getInt("best_time_$categoryId", -1)
        return if (time > 0) time else null
    }

    fun saveBestTimeIfRecord(categoryId: String, timeSeconds: Int): Boolean {
        if (timeSeconds <= 0) return false
        val existing = getBestTime(categoryId)
        if (existing == null || timeSeconds < existing) {
            prefs.edit().putInt("best_time_$categoryId", timeSeconds).apply()
            return true
        }
        return false
    }

    fun getAllBestTimes(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for ((key, value) in prefs.all) {
            if (key.startsWith("best_time_") && value is Int && value > 0) {
                map[key.removePrefix("best_time_")] = value
            }
        }
        return map
    }
}

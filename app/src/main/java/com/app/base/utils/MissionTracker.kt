package com.app.base.utils

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.app.base.database.repository.RewardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Utility class để tracking mission progress từ bất kỳ đâu trong app
 * Sử dụng: MissionTracker.updateProgress(context, "SAVE_PHOTO")
 */
object MissionTracker {

    private fun getRepository(context: Context): RewardRepository {
        return RewardRepository(context)
    }

    private fun getCurrentPlayerId(): Int {
        // Lấy từ SharedPreferences hoặc session
        // Tạm thời return 1
        return 1
    }

    /**
     * Update mission progress với coroutine scope
     */
    fun updateProgress(
        context: Context,
        missionType: String,
        amount: Int = 1,
        scope: CoroutineScope
    ) {
        scope.launch {
            try {
                val repository = getRepository(context)
                val playerId = getCurrentPlayerId()
                repository.updateMissionProgress(playerId, missionType, amount)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    /**
     * Các helper methods cho từng loại mission
     */
    object Mission {
        fun savePhoto(context: Context, scope: CoroutineScope) {
            updateProgress(context, "SAVE_PHOTO", 1, scope)
        }

        fun playMatch(context: Context, scope: CoroutineScope) {
            updateProgress(context, "PLAY_MATCH", 1, scope)
        }

        fun winMatch(context: Context, scope: CoroutineScope) {
            updateProgress(context, "WIN_MATCH", 1, scope)
        }

        fun watchAd(context: Context, scope: CoroutineScope) {
            updateProgress(context, "WATCH_AD", 1, scope)
        }

        fun login(context: Context, scope: CoroutineScope) {
            updateProgress(context, "LOGIN", 1, scope)
        }
    }

    /**
     * Constants cho mission types
     */
    object Type {
        const val SAVE_PHOTO = "SAVE_PHOTO"
        const val PLAY_MATCH = "PLAY_MATCH"
        const val WIN_MATCH = "WIN_MATCH"
        const val WATCH_AD = "WATCH_AD"
        const val LOGIN = "LOGIN"
    }
}

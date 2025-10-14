package com.app.base.core

import android.content.Context
import android.content.SharedPreferences
import com.app.base.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * Evaluate unlock conditions used in assets (e.g. "default", "premium", "mission_1").
 * This helper exposes synchronous methods (internally using runBlocking for DAO calls) so
 * it can be used from existing synchronous repository APIs.
 */
class UnlockChecker(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    private fun getPlayerId(): Int {
        // Keep same default as RewardRepository.getCurrentPlayerId()
        return 1
    }

    fun isUnlockedCondition(condition: String?): Boolean {
        if (condition.isNullOrBlank()) return true
        val cond = condition.trim().lowercase()
        when {
            cond == "default" -> return true
            cond == "always" -> return true
            cond == "premium" -> return prefs.getBoolean("pref_is_premium", false)
            cond.startsWith("mission") -> {
                val playerId = getPlayerId()
                // mission id may be like "mission_1" or "mission001"; take whole string as id
                return runBlocking(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val missionDao = db.missionDao()
                        val mission = missionDao.getMissionById(cond, playerId)
                        if (mission == null) {
                            // try alternative id formats: remove prefix
                            val altId = cond.removePrefix("mission_")
                            val m2 = missionDao.getMissionById(altId, playerId)
                            m2 != null && (m2.status == "COMPLETED" || m2.status == "CLAIMED")
                        } else {
                            mission.status == "COMPLETED" || mission.status == "CLAIMED"
                        }
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            cond.startsWith("owned:") || cond.startsWith("has:") -> {
                // format: owned:<itemType>:<itemId> or has:<itemType>:<itemId>
                val parts = cond.split(":")
                if (parts.size >= 3) {
                    val itemType = parts[1].uppercase()
                    val itemId = parts.subList(2, parts.size).joinToString(":")
                    val playerId = getPlayerId()
                    return runBlocking(Dispatchers.IO) {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val playerItemDao = db.playerItemDao()
                            val count = playerItemDao.hasPlayerItem(playerId, itemId, itemType)
                            count > 0
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
                return false
            }
            else -> {
                // Unknown condition string: as a fallback, treat as unlocked only if pref with that key exists and true
                return prefs.getBoolean(cond, false)
            }
        }
    }
}


package com.app.base.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.lifecycleScope
import com.app.base.database.AppDatabase
import com.app.base.database.entity.PlayerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerManager private constructor(private val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("player_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(context)
    private val playerDao = database.playerDao()

    companion object {
        private const val KEY_CURRENT_PLAYER_ID = "current_player_id"
        private const val KEY_PLAYER_INITIALIZED = "player_initialized"

        @Volatile
        private var INSTANCE: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PlayerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Get current player ID, create default player if none exists
     */
    suspend fun getCurrentPlayerId(): Int = withContext(Dispatchers.IO) {
        val savedPlayerId = sharedPrefs.getInt(KEY_CURRENT_PLAYER_ID, -1)

        if (savedPlayerId != -1) {
            // Check if player still exists in database
            val player = playerDao.getPlayerById(savedPlayerId)
            if (player != null) {
                return@withContext savedPlayerId
            }
        }

        // Create default player if none exists
        return@withContext createDefaultPlayer()
    }

    /**
     * Create a default player for new users
     */
    private suspend fun createDefaultPlayer(): Int = withContext(Dispatchers.IO) {
        val defaultPlayer = PlayerEntity(
            name = "Player",
            diamond = 100, // Start with some diamonds
            itemCount = 0,
            match = 0,
            winrate = 0f,
            avatarId = null,
            rankingPoints = 0
        )

        val playerId = playerDao.insert(defaultPlayer).toInt()

        // Save player ID to SharedPreferences
        sharedPrefs.edit()
            .putInt(KEY_CURRENT_PLAYER_ID, playerId)
            .putBoolean(KEY_PLAYER_INITIALIZED, true)
            .apply()

        return@withContext playerId
    }

    /**
     * Initialize player system - call this on app startup
     */
    suspend fun initializePlayerSystem(): Int = withContext(Dispatchers.IO) {
        // Initialize bots first
        initializeBots()

        return@withContext getCurrentPlayerId()
    }

    /**
     * Initialize bots from JSON into database
     */
    private suspend fun initializeBots() = withContext(Dispatchers.IO) {
        try {
            val botManager = BotManager.getInstance(context)
            botManager.initializeBots()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if player system is initialized
     */
    fun isPlayerInitialized(): Boolean {
        return sharedPrefs.getBoolean(KEY_PLAYER_INITIALIZED, false)
    }

    /**
     * Get current player entity
     */
    suspend fun getCurrentPlayer(): PlayerEntity? = withContext(Dispatchers.IO) {
        val playerId = getCurrentPlayerId()
        return@withContext playerDao.getPlayerById(playerId)
    }

    /**
     * Update player information
     */
    suspend fun updatePlayer(player: PlayerEntity) = withContext(Dispatchers.IO) {
        playerDao.update(player)
    }

    /**
     * Get player's current diamond count
     */
    suspend fun getPlayerDiamonds(): Int = withContext(Dispatchers.IO) {
        val player = getCurrentPlayer()
        return@withContext player?.diamond ?: 0
    }

    /**
     * Add diamonds to current player
     */
    suspend fun addDiamonds(amount: Int) = withContext(Dispatchers.IO) {
        val playerId = getCurrentPlayerId()
        playerDao.addDiamonds(playerId, amount)
    }

    /**
     * Spend diamonds from current player
     */
    suspend fun spendDiamonds(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val playerId = getCurrentPlayerId()
        val result = playerDao.spendDiamonds(playerId, amount)
        return@withContext result > 0
    }

    /**
     * Reset player data (for testing or new game)
     */
    suspend fun resetPlayerData() = withContext(Dispatchers.IO) {
        playerDao.deleteAll()
        sharedPrefs.edit()
            .remove(KEY_CURRENT_PLAYER_ID)
            .remove(KEY_PLAYER_INITIALIZED)
            .apply()

        // Create new default player
        createDefaultPlayer()
    }
}

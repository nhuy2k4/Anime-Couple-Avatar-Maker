package com.app.base.utils

import android.content.Context
import com.app.base.database.AppDatabase
import com.app.base.database.entity.BotEntity
import com.app.base.data.model.BotData
import com.app.base.data.model.BotDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class BotManager private constructor(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val botDao = database.botDao()

    companion object {
        @Volatile
        private var INSTANCE: BotManager? = null

        fun getInstance(context: Context): BotManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BotManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Initialize bots from JSON file into database
     */
    suspend fun initializeBots() = withContext(Dispatchers.IO) {
        try {
            // Check if bots already exist in database
            val existingBotsCount = botDao.getBotsCount()
            if (existingBotsCount > 0) {
                return@withContext // Bots already initialized
            }

            // Load bots from JSON file
            val botEntities = loadBotsFromAssets()
            if (botEntities.isNotEmpty()) {
                botDao.insertAll(botEntities)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Load bots from assets/data/bots.json
     */
    private suspend fun loadBotsFromAssets(): List<BotEntity> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.assets.open("data/bots.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val botsArray = jsonObject.getJSONArray("bots")

            val botEntities = mutableListOf<BotEntity>()

            for (i in 0 until botsArray.length()) {
                val botJson = botsArray.getJSONObject(i)

                val botEntity = BotEntity(
                    id = botJson.getInt("id"),
                    name = botJson.getString("name"),
                    avatarImage = botJson.getString("avatarImage"),
                    difficulty = botJson.getString("difficulty"),
                    maleFeatures = botJson.getJSONObject("maleFeatures").toString(),
                    femaleFeatures = botJson.getJSONObject("femaleFeatures").toString()
                )

                botEntities.add(botEntity)
            }

            return@withContext botEntities

        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    /**
     * Get all bots from database
     */
    suspend fun getAllBots(): List<BotData> = withContext(Dispatchers.IO) {
        val botEntities = botDao.getAllBots()
        return@withContext botEntities.map { convertToBotData(it) }
    }

    /**
     * Get random bot from database
     */
    suspend fun getRandomBot(): BotData? = withContext(Dispatchers.IO) {
        val botEntity = botDao.getRandomBot()
        return@withContext botEntity?.let { convertToBotData(it) }
    }

    /**
     * Get bot by ID from database
     */
    suspend fun getBotById(id: Int): BotData? = withContext(Dispatchers.IO) {
        val botEntity = botDao.getBotById(id)
        return@withContext botEntity?.let { convertToBotData(it) }
    }

    /**
     * Convert BotEntity to BotData
     */
    private fun convertToBotData(entity: BotEntity): BotData {
        val maleFeatures = JSONObject(entity.maleFeatures).let { json ->
            mutableMapOf<String, String>().apply {
                json.keys().forEach { key ->
                    put(key, json.getString(key))
                }
            }
        }

        val femaleFeatures = JSONObject(entity.femaleFeatures).let { json ->
            mutableMapOf<String, String>().apply {
                json.keys().forEach { key ->
                    put(key, json.getString(key))
                }
            }
        }

        val difficulty = when (entity.difficulty) {
            "EASY" -> BotDifficulty.EASY
            "HARD" -> BotDifficulty.HARD
            else -> BotDifficulty.NORMAL
        }

        return BotData(
            id = entity.id,
            name = entity.name,
            avatar = com.app.base.data.model.BotAvatar(
                maleFeatures = maleFeatures,
                femaleFeatures = femaleFeatures
            ),
            difficulty = difficulty
        ).copy(avatarImage = entity.avatarImage)
    }
}

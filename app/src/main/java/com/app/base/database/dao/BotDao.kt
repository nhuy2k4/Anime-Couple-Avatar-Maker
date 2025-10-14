package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.base.database.entity.BotEntity

@Dao
interface BotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bots: List<BotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bot: BotEntity)

    @Query("SELECT * FROM bots")
    suspend fun getAllBots(): List<BotEntity>

    @Query("SELECT * FROM bots WHERE id = :id LIMIT 1")
    suspend fun getBotById(id: Int): BotEntity?

    @Query("SELECT * FROM bots ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomBot(): BotEntity?

    @Query("SELECT COUNT(*) FROM bots")
    suspend fun getBotsCount(): Int

    @Query("DELETE FROM bots")
    suspend fun deleteAll()
}

package com.app.base.data.local

import androidx.room.*
import com.app.base.data.models.Bot

@Dao
interface BotDao {
    @Query("SELECT * FROM bots")
    suspend fun getAllBots(): List<Bot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bot: Bot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bots: List<Bot>)

    @Delete
    suspend fun delete(bot: Bot)
}


package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.PlayerItemEntity

@Dao
interface PlayerItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playerItem: PlayerItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playerItems: List<PlayerItemEntity>)

    @Query("SELECT * FROM player_items WHERE playerId = :playerId")
    suspend fun getPlayerItems(playerId: Int): List<PlayerItemEntity>

    @Query("SELECT * FROM player_items WHERE playerId = :playerId AND itemType = :itemType")
    suspend fun getPlayerItemsByType(playerId: Int, itemType: String): List<PlayerItemEntity>

    @Query("SELECT * FROM player_items WHERE playerId = :playerId AND itemType = :itemType AND category = :category")
    suspend fun getPlayerItemsByTypeAndCategory(playerId: Int, itemType: String, category: String): List<PlayerItemEntity>

    @Query("SELECT COUNT(*) FROM player_items WHERE playerId = :playerId AND itemId = :itemId AND itemType = :itemType")
    suspend fun hasPlayerItem(playerId: Int, itemId: String, itemType: String): Int

    @Query("SELECT COUNT(*) FROM player_items WHERE playerId = :playerId AND itemType = 'FEATURE'")
    suspend fun getPlayerFeatureCount(playerId: Int): Int

    @Query("DELETE FROM player_items WHERE playerId = :playerId AND itemId = :itemId AND itemType = :itemType")
    suspend fun deletePlayerItem(playerId: Int, itemId: String, itemType: String)

    @Query("DELETE FROM player_items WHERE playerId = :playerId")
    suspend fun deleteAllPlayerItems(playerId: Int)

    @Update
    suspend fun update(playerItem: PlayerItemEntity)
}

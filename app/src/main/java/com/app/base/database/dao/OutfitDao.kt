package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.OutfitEntity

@Dao
interface OutfitDao {
    @Insert
    suspend fun insert(outfit: OutfitEntity): Long

    @Query("SELECT * FROM outfits ORDER BY id DESC")
    suspend fun getAllOutfits(): List<OutfitEntity>

    @Query("SELECT * FROM outfits WHERE id = :id LIMIT 1")
    suspend fun getOutfitById(id: Long): OutfitEntity?

    @Query("DELETE FROM outfits WHERE id = :id")
    suspend fun deleteOutfit(id: Long)

    @Update
    suspend fun update(outfit: OutfitEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertAll(outfits: List<OutfitEntity>) // ← thêm cái này
    // 👉 Outfit mới nhất
    @Query("SELECT * FROM outfits ORDER BY id DESC LIMIT 1")
    suspend fun getLatestOutfit(): OutfitEntity?
}
package com.app.base.data.local

import androidx.room.*
import com.app.base.data.models.Outfit

@Dao
interface OutfitDao {
    @Query("SELECT * FROM outfits WHERE id = :id")
    suspend fun getOutfitById(id: Long): Outfit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outfit: Outfit): Long

    @Update
    suspend fun update(outfit: Outfit)

    @Delete
    suspend fun deleteOutfit(outfit: Outfit)
}

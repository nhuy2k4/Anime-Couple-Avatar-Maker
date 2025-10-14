package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.CosplayResultEntity

@Dao
interface CosplayResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cosplayResult: CosplayResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cosplayResults: List<CosplayResultEntity>)

    @Query("SELECT * FROM cosplay_results ORDER BY id DESC")
    suspend fun getAllCosplayResults(): List<CosplayResultEntity>

    @Query("SELECT * FROM cosplay_results WHERE id = :id LIMIT 1")
    suspend fun getCosplayResultById(id: Int): CosplayResultEntity?

    @Query("SELECT * FROM cosplay_results ORDER BY score DESC")
    suspend fun getCosplayResultsByScore(): List<CosplayResultEntity>

    @Query("SELECT * FROM cosplay_results WHERE score >= :minScore")
    suspend fun getCosplayResultsAboveScore(minScore: Float): List<CosplayResultEntity>

    @Query("SELECT AVG(score) FROM cosplay_results")
    suspend fun getAverageScore(): Float?

    @Query("SELECT MAX(score) FROM cosplay_results")
    suspend fun getHighestScore(): Float?

    @Query("SELECT COUNT(*) FROM cosplay_results")
    suspend fun getTotalResults(): Int

    @Query("DELETE FROM cosplay_results WHERE id = :id")
    suspend fun deleteCosplayResult(id: Int)

    @Query("DELETE FROM cosplay_results")
    suspend fun deleteAll()

    @Update
    suspend fun update(cosplayResult: CosplayResultEntity)
}

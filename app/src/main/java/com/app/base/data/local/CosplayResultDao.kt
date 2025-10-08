package com.app.base.data.local

import androidx.room.*
import com.app.base.data.models.CosplayResult

@Dao
interface CosplayResultDao {
    @Query("SELECT * FROM cosplay_results WHERE id = :id")
    suspend fun getResult(id: String): CosplayResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: CosplayResult)

    @Delete
    suspend fun delete(result: CosplayResult)
}


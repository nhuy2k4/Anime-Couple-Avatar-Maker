package com.app.base.data.local

import androidx.room.*
import com.app.base.data.models.Mission

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions")
    suspend fun getAllMissions(): List<Mission>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: Mission)

    @Update
    suspend fun update(mission: Mission)

    @Delete
    suspend fun delete(mission: Mission)
}


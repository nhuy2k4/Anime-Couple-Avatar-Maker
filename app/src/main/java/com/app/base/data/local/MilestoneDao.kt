package com.app.base.data.local

import androidx.room.*
import com.app.base.data.models.Milestone

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones")
    suspend fun getAllMilestones(): List<Milestone>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milestone: Milestone)

    @Update
    suspend fun update(milestone: Milestone)

    @Delete
    suspend fun delete(milestone: Milestone)
}


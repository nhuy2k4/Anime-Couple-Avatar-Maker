package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.MissionEntity

@Dao
interface MissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: MissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(missions: List<MissionEntity>)

    @Query("SELECT * FROM missions WHERE playerId = :playerId AND isDaily = 1 ORDER BY priority ASC")
    suspend fun getDailyMissions(playerId: Int): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE playerId = :playerId AND isDaily = 0 ORDER BY priority ASC")
    suspend fun getAchievements(playerId: Int): List<MissionEntity>

    @Query("SELECT * FROM missions WHERE id = :missionId AND playerId = :playerId LIMIT 1")
    suspend fun getMissionById(missionId: String, playerId: Int): MissionEntity?

    @Query("UPDATE missions SET progress = :progress WHERE id = :missionId AND playerId = :playerId")
    suspend fun updateProgress(missionId: String, playerId: Int, progress: Int)

    @Query("UPDATE missions SET status = :status WHERE id = :missionId AND playerId = :playerId")
    suspend fun updateStatus(missionId: String, playerId: Int, status: String)

    @Query("UPDATE missions SET status = :status, completedDate = :completedDate WHERE id = :missionId AND playerId = :playerId")
    suspend fun markCompleted(missionId: String, playerId: Int, status: String, completedDate: Long)

    @Query("UPDATE missions SET status = 'CLAIMED', claimedDate = :claimedDate WHERE id = :missionId AND playerId = :playerId")
    suspend fun markClaimed(missionId: String, playerId: Int, claimedDate: Long)

    @Query("SELECT COUNT(*) FROM missions WHERE playerId = :playerId AND status = 'COMPLETED' AND isDaily = 1")
    suspend fun getCompletedDailyMissionsCount(playerId: Int): Int

    @Query("SELECT COUNT(*) FROM missions WHERE playerId = :playerId AND isDaily = 1")
    suspend fun getTotalDailyMissionsCount(playerId: Int): Int

    @Query("DELETE FROM missions WHERE playerId = :playerId AND isDaily = 1")
    suspend fun resetDailyMissions(playerId: Int)

    @Update
    suspend fun update(mission: MissionEntity)
}

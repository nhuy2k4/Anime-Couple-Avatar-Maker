package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.PlayerRewardEntity

@Dao
interface PlayerRewardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playerReward: PlayerRewardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playerRewards: List<PlayerRewardEntity>)

    @Query("SELECT * FROM player_rewards WHERE playerId = :playerId ORDER BY createdDate DESC")
    suspend fun getPlayerRewards(playerId: Int): List<PlayerRewardEntity>

    @Query("SELECT * FROM player_rewards WHERE playerId = :playerId AND source = :source ORDER BY createdDate DESC")
    suspend fun getPlayerRewardsBySource(playerId: Int, source: String): List<PlayerRewardEntity>

    @Query("SELECT * FROM player_rewards WHERE playerId = :playerId AND status = :status ORDER BY createdDate DESC")
    suspend fun getPlayerRewardsByStatus(playerId: Int, status: String): List<PlayerRewardEntity>

    @Query("SELECT * FROM player_rewards WHERE playerId = :playerId AND source = 'MISSION' AND status IN ('AVAILABLE', 'IN_PROGRESS', 'COMPLETED') ORDER BY createdDate ASC")
    suspend fun getActiveMissions(playerId: Int): List<PlayerRewardEntity>

    @Query("SELECT * FROM player_rewards WHERE playerId = :playerId AND source = 'MISSION' AND status = 'COMPLETED'")
    suspend fun getCompletedMissions(playerId: Int): List<PlayerRewardEntity>

    @Query("SELECT * FROM player_rewards WHERE id = :rewardId")
    suspend fun getRewardById(rewardId: Long): PlayerRewardEntity?

    @Query("SELECT * FROM player_rewards WHERE playerId = :playerId AND sourceId = :sourceId LIMIT 1")
    suspend fun getRewardBySourceId(playerId: Int, sourceId: String): PlayerRewardEntity?

    @Query("UPDATE player_rewards SET progress = :progress WHERE id = :rewardId")
    suspend fun updateProgress(rewardId: Long, progress: Int)

    @Query("UPDATE player_rewards SET status = :status WHERE id = :rewardId")
    suspend fun updateStatus(rewardId: Long, status: String)

    @Query("UPDATE player_rewards SET status = :status, claimedDate = :claimedDate WHERE id = :rewardId")
    suspend fun claimReward(rewardId: Long, status: String, claimedDate: Long)

    @Query("UPDATE player_rewards SET progress = :progress, status = CASE WHEN :progress >= target THEN 'COMPLETED' ELSE status END WHERE id = :rewardId")
    suspend fun updateProgressAndStatus(rewardId: Long, progress: Int)

    @Query("DELETE FROM player_rewards WHERE playerId = :playerId AND source = :source")
    suspend fun deleteRewardsBySource(playerId: Int, source: String)

    @Update
    suspend fun update(playerReward: PlayerRewardEntity)
}

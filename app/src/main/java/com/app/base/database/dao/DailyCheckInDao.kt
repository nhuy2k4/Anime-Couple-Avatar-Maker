package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.DailyCheckInEntity

@Dao
interface DailyCheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailyCheckIn: DailyCheckInEntity)

    @Query("SELECT * FROM daily_checkin WHERE playerId = :playerId LIMIT 1")
    suspend fun getDailyCheckIn(playerId: Int): DailyCheckInEntity?

    // Alias for compatibility
    suspend fun getByPlayerId(playerId: Int): DailyCheckInEntity? = getDailyCheckIn(playerId)

    @Query("UPDATE daily_checkin SET day = :day, lastCheckInDate = :date, currentStreak = :streak, totalCheckIns = totalCheckIns + 1 WHERE playerId = :playerId")
    suspend fun updateCheckIn(playerId: Int, day: Int, date: String, streak: Int)

    @Query("UPDATE daily_checkin SET currentStreak = 0, lastResetDate = :resetDate WHERE playerId = :playerId")
    suspend fun resetStreak(playerId: Int, resetDate: String)

    @Query("SELECT currentStreak FROM daily_checkin WHERE playerId = :playerId")
    suspend fun getCurrentStreak(playerId: Int): Int?

    @Query("SELECT lastCheckInDate FROM daily_checkin WHERE playerId = :playerId")
    suspend fun getLastCheckInDate(playerId: Int): String?

    @Update
    suspend fun update(dailyCheckIn: DailyCheckInEntity)
}

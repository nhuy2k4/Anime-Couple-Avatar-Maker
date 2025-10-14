package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_checkin")
data class DailyCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerId: Int,
    val day: Int, // 1-7 for consecutive days
    val lastCheckInDate: String, // yyyy-MM-dd format
    val currentStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val lastResetDate: String? = null
)

package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey
    val id: String, // "save_photo", "play_match", "watch_ad", etc.
    val playerId: Int,
    val title: String, // "Save 1 Photo"
    val description: String = "",
    val type: String, // "SAVE_PHOTO", "PLAY_MATCH", "WATCH_AD", "WIN_MATCH"
    val target: Int, // Số lượng cần hoàn thành
    val progress: Int = 0, // Tiến độ hiện tại
    val status: String = "IN_PROGRESS", // "LOCKED", "IN_PROGRESS", "COMPLETED", "CLAIMED"
    val rewardType: String = "DIAMOND", // "DIAMOND", "FEATURE", "BACKGROUND"
    val rewardAmount: Int = 50, // Số kim cương hoặc ID feature
    val rewardData: String = "", // JSON data cho reward phức tạp
    val priority: Int = 1, // Thứ tự hiển thị
    val isDaily: Boolean = true, // true = daily mission, false = achievement
    val createdDate: Long = System.currentTimeMillis(),
    val completedDate: Long? = null,
    val claimedDate: Long? = null
)

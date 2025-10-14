package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_rewards")
data class PlayerRewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerId: Int,
    val source: String, // "DAILY_CHECKIN", "MISSION", "AD", "PK_BOT"
    val sourceId: String, // unique identifier for the reward source
    val rewardType: String, // "DIAMOND", "FEATURE", "BACKGROUND", "AVATAR"
    val rewardData: String, // JSON data for reward details
    val status: String, // "LOCKED", "AVAILABLE", "COMPLETED", "CLAIMED"
    val progress: Int = 0,
    val target: Int = 1,
    val createdDate: Long = System.currentTimeMillis(),
    val claimedDate: Long? = null,
    val description: String = "",
    val title: String = ""
)

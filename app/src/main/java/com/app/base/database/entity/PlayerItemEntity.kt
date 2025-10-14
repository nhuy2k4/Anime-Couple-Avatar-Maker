package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_items")
data class PlayerItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerId: Int,
    val itemType: String, // "FEATURE", "BACKGROUND", "AVATAR", "SKIN"
    val itemId: String,   // ID của item (featureId, backgroundId, etc.)
    val category: String? = null, // "eyes", "hair", "shirt", etc. (cho FEATURE)
    val acquiredDate: Long = System.currentTimeMillis(),
    val source: String = "REWARD" // "REWARD", "PURCHASE", "DEFAULT"
)

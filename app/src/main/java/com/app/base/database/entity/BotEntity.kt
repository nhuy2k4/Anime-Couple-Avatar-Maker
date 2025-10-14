package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val avatarImage: String,
    val difficulty: String, // "EASY", "NORMAL", "HARD"
    val maleFeatures: String, // JSON string
    val femaleFeatures: String // JSON string
)

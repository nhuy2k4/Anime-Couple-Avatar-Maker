package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val diamond: Int = 0,
    val itemCount: Int = 0,
    val match: Int = 0,
    val winrate: Float = 0f,
    val avatarId: String? = null,
    val rankingPoints: Int = 0
)

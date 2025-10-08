package com.app.base.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val name: String = "Player",
    val diamond: Int = 0,
    val match: Int = 0,
    val winrate: Float = 0f,
    val itemCount: Int = 0,
    val rankingPoints: Int = 0,
    val avatarId: String? = null,
    val inventoryFeatures: List<String> = emptyList(),
    val inventoryBackgrounds: List<String> = emptyList()
)


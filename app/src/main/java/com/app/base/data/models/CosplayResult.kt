package com.app.base.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cosplay_results")
data class CosplayResult(
    @PrimaryKey
    val id: String,
    val targetOutfit: String,
    val score: Float,
    val reward: Int
)


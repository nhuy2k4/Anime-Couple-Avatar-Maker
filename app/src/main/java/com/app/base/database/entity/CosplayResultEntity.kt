package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cosplay_results")
data class CosplayResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val targetOutfit: String,   // outfitId or json
    val score: Float,
    val reward: Int
)

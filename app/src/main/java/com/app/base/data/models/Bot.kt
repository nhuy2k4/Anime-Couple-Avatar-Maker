package com.app.base.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class Bot(
    @PrimaryKey
    val id: String,
    val avatar: String,
    val outfitUri: String,
    val point: Int,
    val rank: Int
)


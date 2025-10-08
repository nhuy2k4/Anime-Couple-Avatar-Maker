package com.app.base.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val playerId: Int,
    val features: Map<String, String> = emptyMap(),
    val backgroundId: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)


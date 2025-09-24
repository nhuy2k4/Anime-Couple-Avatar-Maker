package com.app.base.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.net.Uri

@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val outfitJson: String,
    val backgroundUri: String? = null,
    val thumbnailPath: String? = null
)


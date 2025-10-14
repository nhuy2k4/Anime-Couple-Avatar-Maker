package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "features")
data class Feature(
    @PrimaryKey val id: String,
    val category: String, // skin, eyes, hair, shirt,...
    val name: String,
    val gender: String,
    val image: String,     // đường dẫn ảnh trong assets/images/features/...
    val unlockCondition: String? = null
)
package com.app.base.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backgrounds")
data class BackgroundEntity(
    @PrimaryKey val id: String,
    val name: String,
    val image: String,
    val unlockCondition: String? = null
)
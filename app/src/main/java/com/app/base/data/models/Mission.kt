package com.app.base.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missions")
data class Mission(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String,
    val status: String,
    val rewardDiamond: Int? = null,
    val rewardFeatureId: String? = null
)


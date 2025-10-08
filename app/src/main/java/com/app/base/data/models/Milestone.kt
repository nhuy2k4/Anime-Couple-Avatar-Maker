package com.app.base.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey
    val id: String,
    val requiredPoints: Int,
    val rewardDiamond: Int? = null,
    val rewardFeatureId: String? = null,
    val isClaimed: Boolean = false
)


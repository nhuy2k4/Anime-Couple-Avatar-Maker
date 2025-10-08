package com.app.base.data.models

// Feature is a catalog model stored in assets (JSON). Do not treat as a Room entity here.
data class Feature(
    val id: String,
    val category: String,
    val name: String,
    val gender: String? = null,
    val image: String,
    val unlockCondition: String? = null
)

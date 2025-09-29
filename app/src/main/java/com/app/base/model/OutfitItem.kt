package com.app.base.model

data class OutfitItem(
    val id: String,
    val userId: String,
    val features: Map<String, String>,
    val backgroundId: String,
    val timestamp: Long
)


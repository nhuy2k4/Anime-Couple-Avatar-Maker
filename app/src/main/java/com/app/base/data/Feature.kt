package com.app.base.data

data class Feature(
    val id: String,
    val category: String,
    val name: String,
    val gender: String,
    val image: String,
    val unlockCondition: String? = null
)

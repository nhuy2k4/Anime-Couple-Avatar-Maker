package com.app.base.data.models

data class Background(
    val id: String,
    val name: String,
    val image: String,
    val unlockCondition: String? = null
)


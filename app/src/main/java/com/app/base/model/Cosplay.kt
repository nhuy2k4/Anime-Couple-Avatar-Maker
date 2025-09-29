package com.app.base.model

data class Cosplay(
    val id: String,
    val name: String,
    val outfit: Map<String, String>,
    val image: String,
    val unlockCondition: String
)


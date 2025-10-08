package com.app.base.data

data class Cosplay(
    val id: String,
    val name: String,
    val image: String,
    val outfitUri: String,
    val unlockCondition: String? = null
)
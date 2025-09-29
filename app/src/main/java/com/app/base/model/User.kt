package com.app.base.model

data class User(
    val id: String,
    val name: String,
    val avatar: String,
    val diamond: Int,
    val match: Int,
    val winRate: Float,
    val ranking: Int,
    val items: Map<String, Int>
)


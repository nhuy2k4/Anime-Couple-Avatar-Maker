package com.app.base.data
data class Character(
    val id: String,
    val gender: String,
    val outfit: Map<String, String>
)

data class Scene(
    val id: String,
    val name: String?,
    val background: String,
    val characters: List<Character>
)

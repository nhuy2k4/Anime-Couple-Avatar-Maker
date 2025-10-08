package com.app.base.data.models

data class CosplayScene(
    val id: String,
    val name: String,
    val background: String,
    val characters: List<CosplayCharacter>
)


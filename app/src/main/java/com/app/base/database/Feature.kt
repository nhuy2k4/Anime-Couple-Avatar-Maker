package com.app.base.database

data class Feature(
    val id: String,
    val category: String, // skin, eyes, hair, shirt,...
    val name: String,
    val gender: String,
    val image: String,     // đường dẫn ảnh trong assets/images/features/...
    val unlockCondition: String? = null
)
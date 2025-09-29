package com.app.base.core.utils

import android.widget.ImageView

data class FeatureOffset(
    val x: Int,
    val y: Int
)

data class CharacterLayer(
    val id: String,
    val bodyRes: Int,
    val offsetX: Int,
    val offsetY: Int,
    var bodyView: ImageView? = null,
    val features: MutableMap<String, ImageView> = mutableMapOf(),
    val featureOffsets: MutableMap<String, FeatureOffset> = mutableMapOf(), // vị trí offset từng slot
    val bodyOriginalWidth: Int = 0,
    val bodyOriginalHeight: Int = 0
)


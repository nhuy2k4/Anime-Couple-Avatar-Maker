package com.braly.ads.data

data class NativeConfig(
    val unit: String,
    ) {
}

fun String.toIntOrNull(): Int? {
    return try {
        toInt()
    } catch (ex: Exception) {
        null
    }
}
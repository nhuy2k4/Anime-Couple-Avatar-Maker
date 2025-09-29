package com.app.base.utils

import android.content.Context
import android.util.Log

object JsonUtils {
    fun loadJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val json = String(buffer, Charsets.UTF_8)

                Log.d("JsonUtils", "Loaded JSON from $fileName: $json")
                json
            }
        } catch (e: Exception) {
            Log.e("JsonUtils", "Error loading $fileName: ${e.message}")
            null
        }
    }
}

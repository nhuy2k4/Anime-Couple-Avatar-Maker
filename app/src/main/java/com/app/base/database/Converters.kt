package com.app.base.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(map: Map<String, String>?): String? {
        return if (map == null) null else gson.toJson(map)
    }

    @TypeConverter
    fun toMap(json: String?): Map<String, String> {
        if (json.isNullOrEmpty()) return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type)
    }
}

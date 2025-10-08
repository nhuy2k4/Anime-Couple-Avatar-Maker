package com.app.base.data.repositories

import android.content.Context
import com.app.base.data.models.Background
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BackgroundRepository(private val context: Context) {
    fun loadBackgrounds(): List<Background> {
        val json = context.assets.open("data/backgrounds.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Background>>() {}.type
        return Gson().fromJson(json, type)
    }
}


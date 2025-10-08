package com.app.base.data.repositories

import android.content.Context
import com.app.base.data.models.CosplayScene
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CosplayRepository(private val context: Context) {
    fun loadCosplayScenes(): List<CosplayScene> {
        val json = context.assets.open("data/cosplay_scenes.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<CosplayScene>>() {}.type
        return Gson().fromJson(json, type)
    }
}


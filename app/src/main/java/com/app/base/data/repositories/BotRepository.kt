package com.app.base.data.repositories

import android.content.Context
import com.app.base.data.models.Bot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class BotRepository(private val context: Context) {
    fun loadBots(): List<Bot> {
        val json = context.assets.open("data/bots.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Bot>>() {}.type
        return Gson().fromJson(json, type)
    }
}


package com.app.base.database

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FeatureRepository(private val context: Context) {

    fun loadFeatures(category: String): List<Feature> {
        val json = context.assets.open("data/features/$category.json")
            .bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Feature>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun loadAllFeatures(): List<Feature> {
        val categories = listOf(
            "skin", "eyes", "eyebrows", "mouth", "cheeks", "frontHair", "backHair",
            "mask", "glasses", "hairAccessory", "neckAccessory", "earring", "blindfold",
            "shirt", "pants", "dress", "rightHand", "leftHand", "shoes", "wings"
        )

        return categories.flatMap { loadFeatures(it) }
    }
}

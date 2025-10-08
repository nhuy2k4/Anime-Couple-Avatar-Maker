package com.app.base.data.repositories

import android.content.Context
import com.app.base.data.models.Feature
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FeatureRepository(private val context: Context) {

    fun loadFeatures(category: String): List<Feature> {
        val filename = "data/features/$category.json"
        val json = context.assets.open(filename).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Feature>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun loadAllCategories(categories: List<String>): Map<String, List<Feature>> {
        val map = mutableMapOf<String, List<Feature>>()
        categories.forEach { cat ->
            try {
                map[cat] = loadFeatures(cat)
            } catch (e: Exception) {
                map[cat] = emptyList()
            }
        }
        return map
    }
}


package com.app.base.database

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FeatureRepository(private val context: Context) {

    /**
     * Robustly load features for a category name. This will try multiple candidate filenames
     * and both `assets/data/features/<name>.json` and `assets/features/<name>/<name>.json`.
     */
    fun loadFeatures(category: String): List<Feature> {
        // Prioritize canonical asset names to avoid trying incorrect singular forms first
        val normalized = category.trim().lowercase()
        val prioritized = mutableListOf<String>()
        if (normalized.contains("eye")) prioritized.add("eyes")
        if (normalized.contains("hair")) prioritized.add("frontHair")
        // add the original and plural/singular variations
        prioritized.add(category)
        if (category.endsWith("s", true)) prioritized.add(category.removeSuffix("s"))
        else prioritized.add(category + "s")
        // final safe defaults
        prioritized.addAll(listOf("eyes", "frontHair"))
        val candidates = prioritized.map { it.trim() }.distinct()

        val type = object : TypeToken<List<Feature>>() {}.type

        // Try candidates under assets/data/features/<candidate>.json first
        for (cand in candidates) {
            try {
                val path = "data/features/$cand.json"
                context.assets.open(path).bufferedReader().use { reader ->
                    val json = reader.readText()
                    val parsed: List<Feature> = Gson().fromJson(json, type)
                    if (parsed.isNotEmpty()) {
                        Log.d("FeatureRepository", "Loaded features from $path")
                        return parsed
                    }
                }
            } catch (_: Exception) {
                // ignore, we'll try other candidates/locations
            }
        }

        // If not found under data/features, try assets/features/<candidate>/<candidate>.json
        for (cand in candidates) {
            try {
                val path = "features/$cand/$cand.json"
                context.assets.open(path).bufferedReader().use { reader ->
                    val json = reader.readText()
                    val parsed: List<Feature> = Gson().fromJson(json, type)
                    if (parsed.isNotEmpty()) {
                        Log.d("FeatureRepository", "Loaded features from $path")
                        return parsed
                    }
                }
            } catch (_: Exception) {
                // ignore
            }
        }

        // Nothing found — log a single warning to reduce noise
        Log.w("FeatureRepository", "No feature JSON found for category='$category' (tried: ${candidates.joinToString()})")
        return emptyList()
    }

    fun loadAllFeatures(): List<Feature> {
        val categories = listOf(
            "skin", "eyes", "eyebrows", "mouth", "cheeks", "frontHair", "backHair",
            "mask", "glasses", "hairAccessory", "neckAccessory", "earring", "blindfold",
            "shirt", "pants", "dress", "rightHand", "leftHand", "shoes", "wings"
        )

        return categories.flatMap { loadFeatures(it) }
    }

    // --- New helpers to load from the assets/features/<category>/<file>.json locations ---

    /** Load a list of Feature from an exact asset path (e.g. "features/eyes/eyes.json") */
    fun loadFeaturesFromAssetPath(assetPath: String): List<Feature> {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Feature>>() {}.type
        return Gson().fromJson(json, type)
    }

    /** Load eyes.json from assets/features/eyes/eyes.json */
    fun loadEyes(): List<Feature> {
        return try {
            loadFeaturesFromAssetPath("features/eyes/eyes.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Load frontHair.json from assets/features/frontHair/frontHair.json */
    fun loadFrontHair(): List<Feature> {
        return try {
            loadFeaturesFromAssetPath("features/frontHair/frontHair.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Convenience: return both lists in a map */
    fun loadEyesAndFrontHair(): Map<String, List<Feature>> {
        return mapOf(
            "eyes" to loadEyes(),
            "frontHair" to loadFrontHair()
        )
    }

    /** Return true if there are features for the given category that match the gender (or unisex) */
    fun hasFeaturesFor(category: String, gender: String): Boolean {
        val list = loadFeatures(category)
        if (list.isEmpty()) return false
        val lowerGender = gender.trim().lowercase()
        return list.any { f ->
            val g = f.gender?.lowercase() ?: ""
            g == lowerGender || g == "unisex"
        }
    }
}

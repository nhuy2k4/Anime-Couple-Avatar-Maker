package com.app.base.database.repository

import android.content.Context
import android.util.Log
import com.app.base.database.AppDatabase
import com.app.base.database.entity.Feature
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FeatureRepository(private val context: Context) {

    private val gson = Gson()

    /**
     * Load features for a category: prefer Room DB; if DB empty, fallback to assets and seed DB.
     */
    fun loadFeatures(category: String): List<Feature> {
        val normalized = category.trim().lowercase()
        val prioritized = mutableListOf<String>()
        if (normalized.contains("eye")) prioritized.add("eyes")
        if (normalized.contains("hair")) prioritized.add("frontHair")
        prioritized.add(category)
        if (category.endsWith("s", true)) prioritized.add(category.removeSuffix("s"))
        else prioritized.add(category + "s")
        prioritized.addAll(listOf("eyes", "frontHair"))
        val candidates = prioritized.map { it.trim() }.distinct()

        // 1) Try DB first
        try {
            val db = AppDatabase.getInstance(context)
            val dao = db.featureDao()
            // Try querying DB by category; call suspend function in blocking IO context
            val dbList = try {
                kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) { dao.getFeaturesByCategory(category) }
            } catch (_: Exception) { emptyList<Feature>() }
            if (dbList.isNotEmpty()) {
                // Filter by unlockCondition
                val checker = com.app.base.core.UnlockChecker(context)
                val unlocked = dbList.filter { checker.isUnlockedCondition(it.unlockCondition) }
                Log.d("FeatureRepository", "Loaded ${dbList.size} features for '$category' from DB, unlocked=${unlocked.size}")
                return unlocked
            }
        } catch (_: Exception) {
            // ignore and fallback to assets
        }

        // 2) Fallback to assets (same logic as before)
        val type = object : TypeToken<List<Feature>>() {}.type

        for (cand in candidates) {
            try {
                val path = "data/features/$cand.json"
                context.assets.open(path).bufferedReader().use { reader ->
                    val json = reader.readText()
                    val parsed: List<Feature> = gson.fromJson(json, type)
                    if (parsed.isNotEmpty()) {
                        Log.d("FeatureRepository", "Loaded features from $path")
                        trySeedFeaturesToDb(parsed)
                        // filter by unlockCondition
                        val checker = com.app.base.core.UnlockChecker(context)
                        val unlocked = parsed.filter { checker.isUnlockedCondition(it.unlockCondition) }
                        Log.d("FeatureRepository", "Loaded ${parsed.size} from assets, unlocked=${unlocked.size}")
                        return unlocked
                    }
                }
            } catch (_: Exception) { }
        }

        for (cand in candidates) {
            try {
                val path = "features/$cand/$cand.json"
                context.assets.open(path).bufferedReader().use { reader ->
                    val json = reader.readText()
                    val parsed: List<Feature> = gson.fromJson(json, type)
                    if (parsed.isNotEmpty()) {
                        Log.d("FeatureRepository", "Loaded features from $path")
                        trySeedFeaturesToDb(parsed)
                        val checker = com.app.base.core.UnlockChecker(context)
                        val unlocked = parsed.filter { checker.isUnlockedCondition(it.unlockCondition) }
                        Log.d("FeatureRepository", "Loaded ${parsed.size} from assets, unlocked=${unlocked.size}")
                        return unlocked
                    }
                }
            } catch (_: Exception) { }
        }

        Log.w("FeatureRepository", "No feature JSON found for category='$category' (tried: ${candidates.joinToString()})")
        return emptyList()
    }

    fun loadAllFeatures(): List<Feature> {
        val categories = listOf(
            "skin", "eyes", "eyebrows", "mouth", "cheeks", "frontHair", "backHair",
            "mask", "glasses", "hairAccessory", "neckAccessory", "earring", "blindfold",
            "shirt", "pants", "dress", "rightHand", "leftHand", "shoes", "wings"
        )

        // Prefer DB (getAllFeatures) if available
        try {
            val db = AppDatabase.getInstance(context)
            val dao = db.featureDao()
            val allFromDb = try { kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) { dao.getAllFeatures() } } catch (_: Exception) { emptyList<Feature>() }
            if (allFromDb.isNotEmpty()) {
                Log.d("FeatureRepository", "Loaded ${allFromDb.size} features from DB (all categories)")
                return allFromDb
            }
        } catch (_: Exception) { }

        // Else load from assets and seed DB
        val result = categories.flatMap { loadFeatures(it) }
        return result
    }

    private fun trySeedFeaturesToDb(list: List<Feature>) {
        try {
            val db = AppDatabase.getInstance(context)
            val dao = db.featureDao()
            try { kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) { dao.insertAll(list) } } catch (_: Exception) { /* ignore */ }
            Log.d("FeatureRepository", "Seeded ${list.size} features into DB from assets")
        } catch (_: Exception) {
            // if DB unavailable, just continue — repository still works from assets
            Log.w("FeatureRepository", "Failed to seed features to DB (outer): DB unavailable")
        }
    }

    // --- Existing helpers kept as-is ---
    fun loadFeaturesFromAssetPath(assetPath: String): List<Feature> {
        val json = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Feature>>() {}.type
        return gson.fromJson(json, type)
    }

    fun loadEyes(): List<Feature> {
        return try {
            loadFeaturesFromAssetPath("features/eyes/eyes.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadFrontHair(): List<Feature> {
        return try {
            loadFeaturesFromAssetPath("features/frontHair/frontHair.json")
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadEyesAndFrontHair(): Map<String, List<Feature>> {
        return mapOf(
            "eyes" to loadEyes(),
            "frontHair" to loadFrontHair()
        )
    }

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

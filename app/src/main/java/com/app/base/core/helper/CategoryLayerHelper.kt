package com.app.base.core.helper

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.core.layer.DisplayItem
import com.app.base.core.layer.LayerItemAdapter
import com.app.base.databinding.FragmentCategoryBinding
import com.app.base.database.FeatureRepository

class CategoryLayerHelper(
    private val binding: FragmentCategoryBinding,
    private val layerSetupHelper: LayerSetupHelper,
    private val onFeatureUpdated: (String, Int) -> Unit, // callback khi update feature
    private val onCharacterChange: (() -> Unit)? = null // callback khi đổi nhân vật
) {
    private var currentCharacter = "female"
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter
    private val featureRepo = FeatureRepository(binding.root.context)

    /** Thiết lập tab mặc định */
    fun setupDefaultTab() {
        currentFeatureType = "eye"
        showCategoryFeatures(currentFeatureType!!)
    }

    /** Khởi tạo adapter cho feature */
    fun setupFeatureAdapter() {
        featureAdapter = LayerItemAdapter(mutableListOf()) { displayItem -> onFeatureSelected(displayItem) }
        binding.rcvArts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featureAdapter
            visibility = View.GONE
        }
    }

    /** Hiển thị features theo category */
    fun showCategoryFeatures(type: String) {
        currentFeatureType = type

        // Normalize category names used for asset filenames vs UI feature types.
        // UI may pass "eye" or "hair" while assets expect "eyes" and "frontHair".
        val assetCategory = when (type.lowercase()) {
            "eye", "eyes" -> "eyes"
            "hair", "fronthair" -> "frontHair"
            else -> type
        }

        // Try multiple candidate asset category names to be resilient to callers using singular/plural or different names.
        val candidates = listOf(
            assetCategory,
            type,
            if (type.endsWith("s")) type.removeSuffix("s") else type + "s",
            "eyes",
            "frontHair",
            "hair"
        ).map { it.trim() }.distinct()

        var features: List<com.app.base.database.Feature> = emptyList()
        for (candidate in candidates) {
            try {
                features = featureRepo.loadFeatures(candidate)
                if (features.isNotEmpty()) {
                    Log.d("CategoryLayerHelper", "Loaded features from data/features/$candidate.json (candidate)")
                    break
                }
            } catch (e: Exception) {
                Log.w("CategoryLayerHelper", "Failed to load data/features/$candidate.json: ${e.message}")
            }
        }

        // If still empty, try direct asset paths for well-known categories
        val resolvedFeatures = if (features.isEmpty()) {
            when (assetCategory) {
                "eyes" -> featureRepo.loadEyes()
                "frontHair" -> featureRepo.loadFrontHair()
                else -> emptyList()
            }
        } else features

        // Filter by current character's gender (e.g., "female" or "male"); include "unisex"
        val genderFiltered = resolvedFeatures.filter { f ->
            val g = f.gender?.lowercase() ?: ""
            g == currentCharacter.lowercase() || g == "unisex"
        }
        Log.d("CategoryLayerHelper", "showCategoryFeatures(type=$type) -> resolved=${resolvedFeatures.size} filtered=${genderFiltered.size} for character=$currentCharacter")

        // Map Feature.image -> drawable resource id where possible.
        val context = binding.root.context
        val packageName = context.packageName
        val items = genderFiltered.mapNotNull { feature ->
             val imagePath = feature.image
             val name = imagePath.substringAfterLast('/').substringBeforeLast('.')
             val resId = context.resources.getIdentifier(name, "drawable", packageName)
             if (resId != 0) {
                 DisplayItem(resId = resId)
             } else {
                 // If drawable not present, try using asset directly
                 // imagePath is already relative to assets (e.g. "features/eyes/eyes_f_1.png")
                 Log.w("CategoryLayerHelper", "Drawable not found for feature image: $imagePath (resolved name=$name)")
                 DisplayItem(assetPath = imagePath)
             }
         }

         featureAdapter.updateData(items)
         binding.rcvArts.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
     }

    /** Callback khi chọn một feature */
    private fun onFeatureSelected(item: com.app.base.core.layer.DisplayItem) {
        currentFeatureType?.let { type ->
            if (item.resId != null) {
                layerSetupHelper.updateFeature(currentCharacter, type, item.resId)
                Log.d("CategoryLayerHelper", "Selected drawable feature for $currentCharacter: $type -> ${item.resId}")
                onFeatureUpdated(type, item.resId)
            } else if (item.assetPath != null) {
                layerSetupHelper.updateFeatureFromAsset(currentCharacter, type, item.assetPath)
                Log.d("CategoryLayerHelper", "Selected asset feature for $currentCharacter: $type -> ${item.assetPath}")
                // pass -1 to indicate asset; caller can inspect stored outfit if needed
                onFeatureUpdated(type, -1)
            }
        }
    }

    /** Toggle nhân vật male/female */
    fun switchCharacter() {
        currentCharacter = if (currentCharacter == "male") "female" else "male"
        currentFeatureType?.let { showCategoryFeatures(it) }
        onCharacterChange?.invoke()
    }

    /** Public getter for current selected character (gender) */
    fun getCurrentCharacter(): String = currentCharacter

    /** Chọn nhân vật cố định (không đổi nếu đang chọn) */
    fun setCharacter(character: String) {
        if (character != currentCharacter) {
            currentCharacter = character
            currentFeatureType?.let { showCategoryFeatures(it) }
            onCharacterChange?.invoke()
        }
    }
}

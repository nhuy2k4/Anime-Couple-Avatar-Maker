package com.app.base.core.layer

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.databinding.FragmentCategoryBinding

class CategoryLayerHelper(
    private val binding: FragmentCategoryBinding,
    private val layerSetupHelper: LayerSetupHelper,
    private val onFeatureUpdated: (String, Int) -> Unit // 👈 callback
) {
    private var currentCharacter = "female"
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter

    // Data demo, có thể load từ VM / API sau này
    private val dummyData = mapOf(
        "male" to mapOf(
            "hair" to listOf(com.app.base.R.drawable.hair3, com.app.base.R.drawable.hair4),
            "eye" to listOf(com.app.base.R.drawable.eye3, com.app.base.R.drawable.eye4)
        ),
        "female" to mapOf(
            "hair" to listOf(com.app.base.R.drawable.hair1, com.app.base.R.drawable.hair2),
            "eye" to listOf(com.app.base.R.drawable.eye1, com.app.base.R.drawable.eye2)
        )
    )

    fun setupDefaultTab() {
        currentFeatureType = "eye"
        showCategoryFeatures(currentFeatureType!!)
    }

    fun setupFeatureAdapter() {
        featureAdapter = LayerItemAdapter(mutableListOf()) { resId -> onFeatureSelected(resId) }
        binding.rcvArts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featureAdapter
            visibility = View.GONE
        }
    }

    fun showCategoryFeatures(type: String) {
        currentFeatureType = type
        val items = dummyData[currentCharacter]?.get(type).orEmpty()
        featureAdapter.updateData(items)
        binding.rcvArts.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun onFeatureSelected(resId: Int) {
        currentFeatureType?.let { type ->
            // 👉 Cập nhật UI
            layerSetupHelper.updateFeature(currentCharacter, type, resId)
            Log.d("CategoryLayerHelper", "Selected feature for $currentCharacter: $currentFeatureType -> $resId")
            Log.d("CategoryLayerHelper", "Current outfitJson: ${layerSetupHelper.getOutfitJson()}")

            // 👉 Gọi callback để update DB
            onFeatureUpdated(type, resId)
        }
    }

    fun switchCharacter() {
        currentCharacter = if (currentCharacter == "male") "female" else "male"
        currentFeatureType?.let { showCategoryFeatures(it) }
    }
}

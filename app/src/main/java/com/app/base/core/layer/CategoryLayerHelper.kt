package com.app.base.core.layer

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.databinding.FragmentCategoryBinding

class CategoryLayerHelper(
    private val binding: FragmentCategoryBinding,
    private val layerSetupHelper: LayerSetupHelper
) {
    private var currentCharacter = "female"
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter

    // Data demo, có thể load từ VM / API sau này
    private val dummyData = mapOf(
        "male" to mapOf("hair" to listOf(R.drawable.hair3, R.drawable.hair4),
            "eye" to listOf(R.drawable.eye3, R.drawable.eye4)),
        "female" to mapOf("hair" to listOf(R.drawable.hair1, R.drawable.hair2),
            "eye" to listOf(R.drawable.eye1, R.drawable.eye2))
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
            layerSetupHelper.updateFeature(currentCharacter, type, resId)
        }
    }

    fun switchCharacter() {
        currentCharacter = if (currentCharacter == "male") "female" else "male"
        currentFeatureType?.let { showCategoryFeatures(it) }
    }
}

package com.app.base.core.helper

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.core.layer.LayerItemAdapter
import com.app.base.databinding.FragmentCategoryBinding

class CategoryLayerHelper(
    private val binding: FragmentCategoryBinding,
    private val layerSetupHelper: LayerSetupHelper,
    private val onFeatureUpdated: (String, Int) -> Unit, // callback khi update feature
    private val onCharacterChange: (() -> Unit)? = null // callback khi đổi nhân vật
) {
    private var currentCharacter = "female"
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter

    // Data demo
    private val dummyData = mapOf(
        "male" to mapOf(
            "hair" to listOf(R.drawable.hair3, R.drawable.hair4),
            "eye" to listOf(R.drawable.eye3, R.drawable.eye4)
        ),
        "female" to mapOf(
            "hair" to listOf(R.drawable.hair1, R.drawable.hair2),
            "eye" to listOf(R.drawable.eye1, R.drawable.eye2)
        )
    )

    /** Thiết lập tab mặc định */
    fun setupDefaultTab() {
        currentFeatureType = "eye"
        showCategoryFeatures(currentFeatureType!!)
    }

    /** Khởi tạo adapter cho feature */
    fun setupFeatureAdapter() {
        featureAdapter = LayerItemAdapter(mutableListOf()) { resId -> onFeatureSelected(resId) }
        binding.rcvArts.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featureAdapter
            visibility = View.GONE
        }
    }

    /** Hiển thị features theo category */
    fun showCategoryFeatures(type: String) {
        currentFeatureType = type
        val items = dummyData[currentCharacter]?.get(type).orEmpty()
        featureAdapter.updateData(items)
        binding.rcvArts.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
    }

    /** Callback khi chọn một feature */
    private fun onFeatureSelected(resId: Int) {
        currentFeatureType?.let { type ->
            layerSetupHelper.updateFeature(currentCharacter, type, resId)
            Log.d("CategoryLayerHelper", "Selected feature for $currentCharacter: $type -> $resId")
            onFeatureUpdated(type, resId)
        }
    }

    /** Toggle nhân vật male/female */
    fun switchCharacter() {
        currentCharacter = if (currentCharacter == "male") "female" else "male"
        currentFeatureType?.let { showCategoryFeatures(it) }
        onCharacterChange?.invoke()
    }

    /** Chọn nhân vật cố định (không đổi nếu đang chọn) */
    fun setCharacter(character: String) {
        if (character != currentCharacter) {
            currentCharacter = character
            currentFeatureType?.let { showCategoryFeatures(it) }
            onCharacterChange?.invoke()
        }
    }
}

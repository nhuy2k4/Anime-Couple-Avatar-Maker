package com.app.base.ui.category

import android.view.View
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.databinding.FragmentCategoryBinding
import ja.burhanrashid52.photoeditor.PhotoEditor


class CategoryLayerHelper(private val binding: FragmentCategoryBinding) {

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

    private var currentCharacter: String = "female"
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter
    private lateinit var photoEditor: PhotoEditor
    private lateinit var layerManager: LayerManager

    private val selectedFeatures = mutableMapOf(
        "male" to mutableMapOf<String, Int>(),
        "female" to mutableMapOf<String, Int>()
    )

    fun setupFeatureAdapter() {
        featureAdapter = LayerItemAdapter(mutableListOf()) { resId -> onFeatureSelected(resId) }
        binding.rcvArts.apply {
            layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = featureAdapter
            visibility = View.GONE
        }
    }

    fun setupInitialLayers() {
        setupPhotoEditor()
        setupLayerManager()
        setupBodyAndDefaultFeatures()
    }

    private fun setupPhotoEditor() {
        photoEditor = PhotoEditor.Builder(binding.root.context, binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()
    }

    private fun setupLayerManager() {
        layerManager = LayerManager(binding.photoContainer, binding.root.context)
    }

    private fun setupBodyAndDefaultFeatures() {
        binding.photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setFullBodies()
                animateAllLayers()
            }
        })
    }
    private fun setFullBodies(){
        setBodies()
        setDefaultHairAndEyes()
    }
    private fun setBodies() {
        val shiftLeft = 0.15f
        val leftMarginFemale = 0.3f
        layerManager.setLayer("male-body", R.drawable.body_base_male, 1f, 1f, 0f - shiftLeft)
        layerManager.setLayer("female-body", R.drawable.body_base_female, 1f, 1f, leftMarginFemale - shiftLeft)
    }

    private fun setDefaultHairAndEyes() {
        val shiftLeft = 0.15f
        val leftMarginFemale = 0.3f

        listOf("male", "female").forEach { character ->
            val leftMargin = if (character == "male") 0f - shiftLeft else leftMarginFemale - shiftLeft
            setDefaultFeature(character, "hair", dummyData[character]?.get("hair")?.firstOrNull(), leftMargin)
            setDefaultFeature(character, "eye", dummyData[character]?.get("eye")?.firstOrNull(), leftMargin)
        }
    }

    private fun setDefaultFeature(character: String, type: String, resId: Int?, leftMargin: Float) {
        resId?.let {
            layerManager.setLayer("$character-$type", it, 1f, 1f, leftMargin)
            selectedFeatures[character]?.put(type, it)
        }
    }

    private fun animateAllLayers() {
        val allLayers = layerManager.getLayersForCharacter("male") + layerManager.getLayersForCharacter("female")
        allLayers.forEach { view ->
            view.scaleX = 0.9f
            view.scaleY = 0.9f
            view.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        }
    }

    private fun onFeatureSelected(resId: Int) {
        currentFeatureType?.let { type ->
            val leftMargin = if (currentCharacter == "male") 0f - 0.15f else 0.15f
            val layerType = "$currentCharacter-$type"
            layerManager.setLayer(layerType, resId, 1f, 1f, leftMargin)
            selectedFeatures[currentCharacter]?.set(type, resId)
        }
    }

    fun showCategoryFeatures(type: String) {
        currentFeatureType = type
        dummyData[currentCharacter]?.get(type)?.let { items -> showFeatureItems(type, items) }
    }

    private fun showFeatureItems(type: String, items: List<Int>) {
        currentFeatureType = type
        featureAdapter.updateData(items)
        restoreSelectedFeature(type)
        binding.rcvArts.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun restoreSelectedFeature(type: String) {
        selectedFeatures[currentCharacter]?.get(type)?.let { resId ->
            val layerType = "$currentCharacter-$type"
            val leftMargin = if (currentCharacter == "male") 0f - 0.15f else 0.15f
            layerManager.setLayer(layerType, resId, 1f, 1f, leftMargin)
        }
    }

    fun switchCharacter() {
        currentCharacter = if (currentCharacter == "male") "female" else "male"
        currentFeatureType?.let { type ->
            dummyData[currentCharacter]?.get(type)?.let { showFeatureItems(type, it) }
        }
        animateCharacterLayers()
    }

    private fun animateCharacterLayers() {
        val layers = layerManager.getLayersForCharacter(currentCharacter)
        layers.forEach { view ->
            view.scaleX = 1f
            view.scaleY = 1f
            view.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
                .withEndAction { view.animate().scaleX(1f).scaleY(1f).setDuration(150).start() }
                .start()
        }
    }

    fun resetToInitialState() {
        // Xóa tất cả layer, kể cả feature, nhưng sau đó thêm lại body + hair + eye mặc định
        layerManager.clearLayers(keepBase = false)

       setFullBodies()
    }

    fun getAllOutfits(): Map<String, Map<String, Int>> {
        return selectedFeatures.mapValues { it.value.toMap() }
    }

}


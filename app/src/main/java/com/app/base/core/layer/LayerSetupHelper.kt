package com.app.base.core.layer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.app.base.R
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import org.json.JSONObject

data class CharacterConfig(
    val baseResId: Int,
    val defaultFeatures: Map<String, Int>,
    val leftMargin: Float
)

class LayerSetupHelper(
    private val photoContainer: FrameLayout,
    private val photoEditorView: PhotoEditorView,
    private val context: Context
) {

    lateinit var photoEditor: PhotoEditor
        private set
    lateinit var layerManager: LayerManager
        private set

    private var outfitJson: JSONObject = JSONObject()

    // Config cho từng nhân vật
    private val characterConfigs = mapOf(
        "male" to CharacterConfig(
            baseResId = R.drawable.body_base_male,
            defaultFeatures = mapOf("hair" to R.drawable.hair3, "eye" to R.drawable.eye3),
            leftMargin = -0.15f
        ),
        "female" to CharacterConfig(
            baseResId = R.drawable.body_base_female,
            defaultFeatures = mapOf("hair" to R.drawable.hair1, "eye" to R.drawable.eye1),
            leftMargin = 0.15f
        )
    )
    /** Setup PhotoEditor + LayerManager + Default Layers */
    fun setupInitialLayers(onReady: (() -> Unit)? = null) {
        photoEditor = PhotoEditor.Builder(context, photoEditorView)
            .setPinchTextScalable(true)
            .build()
        layerManager = LayerManager(photoContainer, context)

        photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                characterConfigs.forEach { (char, config) ->
                    layerManager.setLayer("${char}-body", config.baseResId, 1f, 1f, config.leftMargin)
                    outfitJson.put(char, JSONObject(config.defaultFeatures))
                }
                applyFeaturesToLayers()
                onReady?.invoke()
            }
        })
    }
    fun getOutfitJsonWithBackground(backgroundId: Int?): String {
        val root = JSONObject(getOutfitJson())
        val bgId = backgroundId ?: R.drawable.bg_gradient // fallback
        root.put("backgroundId", bgId)
        return root.toString()
    }


    fun setOutfitJsonWithBackground(json: String) {
        val root = JSONObject(json)

        // load outfit layers
        setOutfitJson(json)

        // load background nếu có
        if (root.has("backgroundId")) {
            val bgId = root.getInt("backgroundId")
            val drawable = ContextCompat.getDrawable(context, bgId)
            photoEditorView.background = drawable
        }
    }

    /** Apply feature layer từ outfitJson */
    fun applyFeaturesToLayers() {
        characterConfigs.forEach { (char, config) ->
            val features = outfitJson.optJSONObject(char) ?: return@forEach
            features.keys().forEach { type ->
                val resId = features.optInt(type, 0)
                if (resId != 0) {
                    val layerKey = "$char-$type"
                    if (layerManager.hasLayer(layerKey)) layerManager.removeLayer(layerKey)
                    layerManager.setLayer(layerKey, resId, 1f, 1f, config.leftMargin)
                }
            }
        }
    }

    /** Update 1 feature cho 1 character */
    fun updateFeature(character: String, type: String, resId: Int) {
        val charFeatures = outfitJson.optJSONObject(character) ?: JSONObject().also {
            outfitJson.put(character, it)
        }
        charFeatures.put(type, resId)
        applyFeaturesToLayers()
    }
    fun getBitmapOfContainer(): Bitmap {
        val width = photoContainer.width
        val height = photoContainer.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        photoContainer.draw(canvas)
        return bitmap
    }

    fun resetToInitialState() {
        layerManager.clearLayers(keepBase = false)
        characterConfigs.forEach { (char, config) ->
            layerManager.setLayer("${char}-body", config.baseResId, 1f, 1f, config.leftMargin)
            outfitJson.put(char, JSONObject(config.defaultFeatures))
        }
        applyFeaturesToLayers()
    }

    /** Lưu / Load outfit JSON */
    fun setOutfitJson(json: String) {
        outfitJson = JSONObject(json)
        applyFeaturesToLayers()
        Log.d("LayerSetupHelper", "setOutfitJson called with: $json")

    }

    fun getOutfitJson(): String = outfitJson.toString()
}
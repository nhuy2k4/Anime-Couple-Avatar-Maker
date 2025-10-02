package com.app.base.core.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.app.base.R
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import org.json.JSONObject
import android.util.Log
import com.app.base.core.layer.LayerManager

data class CharacterConfig(
    val baseResId: Int,
    val defaultFeatures: Map<String, Int>,
    val leftMargin: Float
)

class LayerSetupHelper(
    val photoContainer: FrameLayout,
    private val photoEditorView: PhotoEditorView,
    private val context: Context
) {
    lateinit var photoEditor: PhotoEditor
        private set
    lateinit var layerManager: LayerManager
        private set

    private var outfitJson: JSONObject = JSONObject()

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

    fun initLayerManager() {
        if (!::layerManager.isInitialized) layerManager = LayerManager(photoContainer, context)
        if (!::photoEditor.isInitialized) {
            photoEditor = PhotoEditor.Builder(context, photoEditorView)
                .setPinchTextScalable(true)
                .build()
        }
    }

    fun setupInitialLayers(onReady: (() -> Unit)? = null) {
        initLayerManager()

        photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                characterConfigs.forEach { (char, config) ->
                    val bodyKey = "$char-body"
                    if (!layerManager.hasLayer(bodyKey)) {
                        layerManager.setLayer(bodyKey, config.baseResId, 1f, 1f, config.leftMargin)
                    }

                    if (!outfitJson.has(char)) outfitJson.put(char, JSONObject(config.defaultFeatures))
                }

                applyFeaturesToLayers()
                onReady?.invoke()
            }
        })
    }

    fun setOutfitJsonWithBackground(json: String, replace: Boolean = false) {
        val root = JSONObject(json)
        val bgIdFromJson = if (root.has("backgroundId")) root.getInt("backgroundId") else null

        if (replace) outfitJson = JSONObject()
        mergeOutfitJson(root)

        // Chỉ set background nếu JSON có backgroundId
        bgIdFromJson?.let {
            photoEditorView.background = ContextCompat.getDrawable(context, it)
            outfitJson.put("backgroundId", it)
        }

        applyFeaturesToLayers()
    }


    fun mergeOutfitJson(newJson: JSONObject) {
        val merged = JSONObject(outfitJson.toString())
        newJson.keys().forEach { key ->
            val value = newJson.get(key)
            if (value is JSONObject) {
                val existing = merged.optJSONObject(key) ?: JSONObject()
                value.keys().forEach { subKey -> existing.put(subKey, value.get(subKey)) }
                merged.put(key, existing)
            } else {
                merged.put(key, value)
            }
        }
        Log.d("LayerSetupHelper", "Before merge: $outfitJson")
        Log.d("LayerSetupHelper", "Merging: $newJson")
        outfitJson = merged
        Log.d("LayerSetupHelper", "After merge: $outfitJson")

        Log.d("LayerSetupHelper", "mergeOutfitJson result: $outfitJson")
    }

    fun applyFeaturesToLayers() {
        initLayerManager()
        characterConfigs.forEach { (char, config) ->
            // đảm bảo body luôn tồn tại
            val bodyKey = "$char-body"
            if (!layerManager.hasLayer(bodyKey)) {
                layerManager.setLayer(bodyKey, config.baseResId, 1f, 1f, config.leftMargin)
            }

            val features = outfitJson.optJSONObject(char) ?: return@forEach

            // Remove feature cũ, giữ body
            layerManager.getLayersForCharacter(char).forEach { layerView ->
                val key = layerManager.getKeyForLayerView(layerView)
                if (key != null && key != bodyKey) {
                    layerManager.removeLayer(key)
                }
            }

            Log.d("LayerSetupHelper", "Applying features for character: $char")
            // Set lại feature mới
            features.keys().forEach { type ->
                val resId = features.optInt(type, 0)
                Log.d("LayerSetupHelper", " -> feature=$type, resId=$resId, layerExists=${layerManager.hasLayer("$char-$type")}")
                if (resId != 0) {
                    val layerKey = "$char-$type"
                    layerManager.setLayer(layerKey, resId, 1f, 1f, config.leftMargin)
                }
            }
        }
    }


    fun updateFeature(character: String, type: String, resId: Int) {
        val layerKey = "$character-$type"
        if (layerManager.hasLayer(layerKey)) layerManager.removeLayer(layerKey)

        val charFeatures = outfitJson.optJSONObject(character) ?: JSONObject().also {
            outfitJson.put(character, it)
        }
        charFeatures.put(type, resId)

        val leftMargin = characterConfigs[character]?.leftMargin ?: 0f
        layerManager.setLayer(layerKey, resId, 1f, 1f, leftMargin)
        Log.d("LayerSetupHelper", "Updating feature: $character-$type -> $resId")
        Log.d("LayerSetupHelper", "Current layers: ${layerManager.getLayersForCharacter(character).map { it.id }}")

    }

    fun saveCurrentOutfitToGallery(onDone: (Uri?) -> Unit) {
        val bitmap = renderOutfitBitmap()
        val resolver = context.contentResolver
        try {
            val fileName = "outfit_${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            onDone(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onDone(null)
        }
    }


    fun getBitmapOfContainer(): Bitmap {
        val bitmap = Bitmap.createBitmap(photoContainer.width, photoContainer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        photoContainer.draw(canvas)
        return bitmap
    }

    fun getFullOutfitJson(backgroundId: Int? = null, backgroundUri: Uri? = null): String {
        val root = JSONObject(getOutfitJson())
        backgroundId?.let { root.put("backgroundId", it) }
        backgroundUri?.let { root.put("backgroundUri", it.toString()) }
        return root.toString()
    }

    fun renderOutfitBitmap(): Bitmap = getBitmapOfContainer()

    fun resetToInitialState() {
        initLayerManager()
        layerManager.clearLayers(keepBase = false)
        characterConfigs.forEach { (char, config) ->
            val bodyKey = "$char-body"
            layerManager.setLayer(bodyKey, config.baseResId, 1f, 1f, config.leftMargin)
            outfitJson.put(char, JSONObject(config.defaultFeatures))
        }
        applyFeaturesToLayers()
    }

    fun setOutfitJson(json: String) {
        initLayerManager()
        outfitJson = JSONObject(json)
        applyFeaturesToLayers()
    }

    fun getOutfitJson(): String = outfitJson.toString()
}

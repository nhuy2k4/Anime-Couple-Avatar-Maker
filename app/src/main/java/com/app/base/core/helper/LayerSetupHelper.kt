package com.app.base.core.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.app.base.R
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import org.json.JSONObject
import com.app.base.core.layer.LayerManager

data class CharacterConfig(
    val baseResId: Int,
    val defaultFeatures: Map<String, Int>,
    val leftMargin: Float,
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
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

    val characterConfigs = mapOf(
        "male" to CharacterConfig(
            baseResId = R.drawable.body_base_male,
            defaultFeatures = mapOf("hair" to R.drawable.hair3, "eye" to R.drawable.eye3),
            leftMargin = 0f,
            scale = 0.7f,
            offsetX = 100f,
            offsetY = 250f
        ),
        "female" to CharacterConfig(
            baseResId = R.drawable.body_base_female,
            defaultFeatures = mapOf("hair" to R.drawable.hair1, "eye" to R.drawable.eye1),
            leftMargin = 0f,
            scale = 0.7f,
            offsetX = 200f,
            offsetY = 250f
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

    fun setupInitialLayers(
        characters: List<String>,
        offsetsX: Map<String, Float> = emptyMap(),
        onReady: (() -> Unit)? = null
    ) {
        initLayerManager()
        photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                characters.forEach { char ->
                    val config = characterConfigs[char] ?: return@forEach
                    val bodyKey = "$char-body"

                    val offsetX = offsetsX[char] ?: config.offsetX
                    val offsetY = config.offsetY

                    // body
                    layerManager.setLayer(
                        bodyKey,
                        config.baseResId,
                        scaleX = config.scale,
                        scaleY = config.scale,
                        offsetX = offsetX,
                        offsetY = offsetY
                    )

                    if (!outfitJson.has(char)) outfitJson.put(char, JSONObject(config.defaultFeatures))
                }

                applyFeaturesToLayers(forCharacters = characters, offsetsX = offsetsX)
                onReady?.invoke()
            }
        })
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
    }

    fun getFeaturesMap(): Map<String, Map<String, Int>> {
        val result = mutableMapOf<String, MutableMap<String, Int>>()
        val keys = outfitJson.keys()
        while (keys.hasNext()) {
            val charKey = keys.next()
            val obj = outfitJson.optJSONObject(charKey) ?: continue
            val inner = mutableMapOf<String, Int>()
            val innerKeys = obj.keys()
            while (innerKeys.hasNext()) {
                val k = innerKeys.next()
                inner[k] = obj.optInt(k, 0)
            }
            result[charKey] = inner
        }
        return result
    }

    fun setFeaturesMap(map: Map<String, Map<String, Int>>) {
        val root = JSONObject()
        map.forEach { (char, inner) ->
            val obj = JSONObject()
            inner.forEach { (k, v) -> obj.put(k, v) }
            root.put(char, obj)
        }
        mergeOutfitJson(root)
        applyFeaturesToLayers()
    }

    fun replaceFeaturesMap(map: Map<String, Map<String, Int>>) {
        val root = JSONObject()
        map.forEach { (char, inner) ->
            val obj = JSONObject()
            inner.forEach { (k, v) -> obj.put(k, v) }
            root.put(char, obj)
        }
        outfitJson = root
        applyFeaturesToLayers()
    }

    fun getFeaturesAsStringMap(): Map<String, String> {
        val flat = mutableMapOf<String, String>()
        val keys = outfitJson.keys()
        while (keys.hasNext()) {
            val charKey = keys.next()
            val obj = outfitJson.optJSONObject(charKey) ?: continue
            val innerKeys = obj.keys()
            while (innerKeys.hasNext()) {
                val k = innerKeys.next()
                flat["$charKey.$k"] = obj.optInt(k, 0).toString()
            }
        }
        return flat
    }

    fun applyFeaturesToLayers(
        forCharacters: List<String>? = null,
        offsetsX: Map<String, Float> = emptyMap()
    ) {
        initLayerManager()
        val charsToApply = forCharacters ?: characterConfigs.keys.toList()

        charsToApply.forEach { char ->
            val config = characterConfigs[char] ?: return@forEach
            val bodyKey = "$char-body"

            // lấy vị trí body hiện tại
            val bodyView = layerManager.getLayersForCharacter(char)
                .find { layerManager.getKeyForLayerView(it) == bodyKey }
            val bodyOffsetX = bodyView?.translationX ?: (offsetsX[char] ?: config.offsetX)
            val bodyOffsetY = bodyView?.translationY ?: config.offsetY

            val features = outfitJson.optJSONObject(char) ?: return@forEach

            // xoá các feature cũ
            layerManager.getLayersForCharacter(char).forEach { layerView ->
                val key = layerManager.getKeyForLayerView(layerView)
                if (key != null && key != bodyKey) layerManager.removeLayer(key)
            }

            features.keys().forEach { type ->
                val resId = features.optInt(type, 0)
                if (resId != 0) {
                    val layerKey = "$char-$type"
                    layerManager.setLayer(
                        layerKey,
                        resId,
                        scaleX = config.scale,
                        scaleY = config.scale,
                        offsetX = bodyOffsetX, // feature theo X của body
                        offsetY = bodyOffsetY  // feature theo Y của body
                    )
                }
            }
        }
    }

    fun moveCharacterX(char: String, offsetX: Float) {
        val bodyKey = "$char-body"
        val bodyView = layerManager.getLayersForCharacter(char)
            .find { layerManager.getKeyForLayerView(it) == bodyKey } ?: return
        val deltaX = offsetX - bodyView.translationX
        layerManager.getLayersForCharacter(char).forEach { layer ->
            layer.translationX += deltaX
        }
    }

    fun updateFeature(character: String, type: String, resId: Int) {
        val layerKey = "$character-$type"
        if (layerManager.hasLayer(layerKey)) layerManager.removeLayer(layerKey)

        val charFeatures = outfitJson.optJSONObject(character) ?: JSONObject().also {
            outfitJson.put(character, it)
        }
        charFeatures.put(type, resId)

        val config = characterConfigs[character] ?: CharacterConfig(0, emptyMap(), 0f)
        val bodyView = layerManager.getLayersForCharacter(character)
            .find { layerManager.getKeyForLayerView(it) == "$character-body" }

        val offsetX = bodyView?.translationX ?: config.offsetX
        val offsetY = bodyView?.translationY ?: config.offsetY

        layerManager.setLayer(
            layerKey,
            resId,
            scaleX = config.scale,
            scaleY = config.scale,
            offsetX = offsetX,
            offsetY = offsetY
        )
        Log.d("LayerSetupHelper", "Updated feature: $character-$type -> $resId")
    }

    fun setOutfitJson(json: String) {
        initLayerManager()
        outfitJson = JSONObject(json)
        applyFeaturesToLayers()
    }

    fun setOutfitJsonWithBackground(json: String, replace: Boolean = false) {
        val root = JSONObject(json)
        if (replace) outfitJson = JSONObject()
        mergeOutfitJson(root)

        root.optInt("backgroundId").takeIf { it != 0 }?.let {
            photoEditorView.background = ContextCompat.getDrawable(context, it)
            outfitJson.put("backgroundId", it)
        }

        applyFeaturesToLayers()
    }

    fun getOutfitJson(): String = outfitJson.toString()

    fun getFullOutfitJson(backgroundId: Int? = null, backgroundUri: Uri? = null): String {
        val root = JSONObject(getOutfitJson())
        backgroundId?.let { root.put("backgroundId", it) }
        backgroundUri?.let { root.put("backgroundUri", it.toString()) }
        return root.toString()
    }

    fun getBitmapOfContainer(): Bitmap {
        val bitmap = Bitmap.createBitmap(photoContainer.width, photoContainer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        photoContainer.draw(canvas)
        return bitmap
    }

    fun renderOutfitBitmap(): Bitmap = getBitmapOfContainer()

    fun saveCurrentOutfitToGallery(onDone: (Uri?) -> Unit) {
        val bitmap = renderOutfitBitmap()
        val resolver = context.contentResolver
        try {
            val fileName = "outfit_${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            onDone(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onDone(null)
        }
    }

    fun resetToInitialState() {
        initLayerManager()
        layerManager.clearLayers(keepBase = false)
        characterConfigs.forEach { (char, config) ->
            val bodyKey = "$char-body"
            layerManager.setLayer(
                bodyKey,
                config.baseResId,
                scaleX = config.scale,
                scaleY = config.scale,
                offsetX = config.offsetX,
                offsetY = config.offsetY
            )
            outfitJson.put(char, JSONObject(config.defaultFeatures))
        }
        applyFeaturesToLayers()
    }
}

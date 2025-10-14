package com.app.base.core.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
import com.app.base.database.repository.FeatureRepository

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

    // Feature repository to load feature JSONs from assets
    private val featureRepo = FeatureRepository(context)
    private var defaultsLoaded = false

    val characterConfigs = mapOf(
        "male" to CharacterConfig(
            baseResId = R.drawable.body_base_male,
            defaultFeatures = emptyMap(),
            leftMargin = 0f,
            scale = 0.7f,
            offsetX = 100f,
            offsetY = 250f
        ),
        "female" to CharacterConfig(
            baseResId = R.drawable.body_base_female,
            defaultFeatures = emptyMap(),
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

    private fun ensureDefaultFeaturesLoaded() {
        if (defaultsLoaded) return
        defaultsLoaded = true

        // Load lists (try data/features first, then fallback)
        val eyes = try {
            featureRepo.loadFeatures("eyes")
        } catch (e: Exception) {
            featureRepo.loadEyes()
        }
        val hairs = try {
            featureRepo.loadFeatures("frontHair")
        } catch (e: Exception) {
            featureRepo.loadFrontHair()
        }

        val packageName = context.packageName
        val res = context.resources

        characterConfigs.keys.forEach { char ->
            // Only set defaults if not already present in outfitJson
            if (outfitJson.has(char)) return@forEach

            val gender = if (char == "female") "female" else "male"
            val defaults = JSONObject()

            // pick eye
            val eyes = eyes.firstOrNull { it.gender == gender } ?: eyes.firstOrNull()
            eyes?.let {
                val name = it.image.substringAfterLast('/').substringBeforeLast('.')
                val resId = res.getIdentifier(name, "drawable", packageName)
                if (resId != 0) defaults.put("eyes", resId)
            }

            // pick hair (mapped to key "hair")
            val hair = hairs.firstOrNull { it.gender == gender } ?: hairs.firstOrNull()
            hair?.let {
                val name = it.image.substringAfterLast('/').substringBeforeLast('.')
                val resId = res.getIdentifier(name, "drawable", packageName)
                if (resId != 0) defaults.put("frontHair", resId)
            }

            Log.d("LayerSetupHelper", "Defaults for $char -> eyes=${defaults.optInt("eyes",0)} frontHair=${defaults.optInt("hair",0)}")

             outfitJson.put(char, defaults)
         }
     }

     fun setupInitialLayersFor(characters: List<String>, onReady: (() -> Unit)? = null) {
        initLayerManager()
        photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // ensure defaults loaded from JSON assets
                ensureDefaultFeaturesLoaded()

                characters.forEach { char ->
                    val config = characterConfigs[char] ?: return@forEach
                    val bodyKey = "$char-body"
                    if (!layerManager.hasLayer(bodyKey)) {
                        layerManager.setLayer(
                            bodyKey,
                            config.baseResId,
                            1f,
                            1f,
                            config.leftMargin,
                            scaleX = config.scale,
                            scaleY = config.scale,
                            offsetX = config.offsetX,
                            offsetY = config.offsetY
                        )
                    }

                    // defaults are loaded into outfitJson by ensureDefaultFeaturesLoaded(); nothing to add here
                }

                applyFeaturesToLayers()
                onReady?.invoke()
            }
        })
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

                // ensure defaults loaded from JSON assets
                ensureDefaultFeaturesLoaded()

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

                    // outfitJson should already contain defaults from ensureDefaultFeaturesLoaded(); don't overwrite with empty defaults
                    if (!outfitJson.has(char)) {
                        // As a fallback, set an empty object so subsequent code doesn't NPE
                        outfitJson.put(char, JSONObject())
                    }
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
    fun setBackgroundFromAsset(assetPath: String) {
        try {
            // Try as drawable resource name first (e.g., "test_stage" -> R.drawable.test_stage)
            val packageName = context.packageName
            val resId = try {
                context.resources.getIdentifier(assetPath, "drawable", packageName)
            } catch (t: Throwable) {
                0
            }

            if (resId != 0) {
                Log.d("LayerSetupHelper", "setBackgroundFromAsset: found drawable resource id=$resId for path='$assetPath'")
                photoEditorView.background = ContextCompat.getDrawable(context, resId)
                outfitJson.put("backgroundId", resId)
                return
            }

            // Fallback: try loading from assets
            Log.d("LayerSetupHelper", "setBackgroundFromAsset: attempting to load asset '$assetPath'")
            val drawable = Drawable.createFromStream(context.assets.open(assetPath), null)
            if (drawable != null) {
                photoEditorView.background = drawable
                outfitJson.put("backgroundId", assetPath) // lưu path vào JSON as string
                Log.d("LayerSetupHelper", "Background set from asset: $assetPath")
            } else {
                Log.e("LayerSetupHelper", "Drawable.createFromStream returned null for asset: $assetPath")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("LayerSetupHelper", "Failed to load background from asset: $assetPath")
        }
    }

    fun setFeaturesMap(map: Map<String, Map<String, String>>) {
        val root = JSONObject()
        map.forEach { (char, inner) ->
            val obj = JSONObject()
            inner.forEach { (k, v) -> obj.put(k, v) } // giữ nguyên string path
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
                val v = obj.opt(k)
                flat["$charKey.$k"] = v?.toString() ?: "0"
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

        Log.d("LayerSetupHelper", "=== Starting applyFeaturesToLayers ===")
        Log.d("LayerSetupHelper", "Characters to apply: $charsToApply")
        Log.d("LayerSetupHelper", "Current outfitJson: $outfitJson")

        charsToApply.forEach { char ->
            val config = characterConfigs[char] ?: return@forEach
            val bodyKey = "$char-body"

            Log.d("LayerSetupHelper", "Processing character: $char")

            // lấy vị trí body hiện tại
            val bodyView = layerManager.getLayersForCharacter(char)
                .find { layerManager.getKeyForLayerView(it) == bodyKey }
            val bodyOffsetX = bodyView?.translationX ?: (offsetsX[char] ?: config.offsetX)
            val bodyOffsetY = bodyView?.translationY ?: config.offsetY

            Log.d("LayerSetupHelper", "Body position - X: $bodyOffsetX, Y: $bodyOffsetY")

            val features = outfitJson.optJSONObject(char)
            if (features == null) {
                Log.w("LayerSetupHelper", "No features found for character: $char")
                return@forEach
            }

            Log.d("LayerSetupHelper", "Features for $char: $features")

            // xoá các feature cũ
            val oldLayers = layerManager.getLayersForCharacter(char)
            Log.d("LayerSetupHelper", "Removing ${oldLayers.size} old layers for $char")
            oldLayers.forEach { layerView ->
                val key = layerManager.getKeyForLayerView(layerView)
                if (key != null && key != bodyKey) {
                    Log.d("LayerSetupHelper", "Removing old layer: $key")
                    layerManager.removeLayer(key)
                }
            }

            features.keys().forEach { type ->
                val value = features.opt(type)
                Log.d("LayerSetupHelper", "Processing feature: $type = $value (type: ${value?.javaClass?.simpleName})")

                when (value) {
                    is Number -> {
                        val resId = value.toInt()
                        if (resId != 0) {
                            val layerKey = "$char-$type"
                            Log.d("LayerSetupHelper", "Setting resource layer: $layerKey with resId: $resId")
                            layerManager.setLayer(
                                layerKey,
                                resId,
                                scaleX = config.scale,
                                scaleY = config.scale,
                                offsetX = bodyOffsetX,
                                offsetY = bodyOffsetY
                            )
                            Log.d("LayerSetupHelper", "Successfully set resource layer: $layerKey")
                        } else {
                            Log.w("LayerSetupHelper", "Skipping zero resId for $char-$type")
                        }
                    }
                    is String -> {
                        val assetPath = value
                        val layerKey = "$char-$type"
                        Log.d("LayerSetupHelper", "Setting asset layer: $layerKey from path: $assetPath")

                        try {
                            layerManager.setLayerFromAsset(
                                layerKey,
                                assetPath,
                                scaleX = config.scale,
                                scaleY = config.scale,
                                offsetX = bodyOffsetX,
                                offsetY = bodyOffsetY
                            )
                            Log.d("LayerSetupHelper", "Successfully set asset layer: $layerKey")
                        } catch (e: Exception) {
                            Log.e("LayerSetupHelper", "Failed to set asset layer $layerKey: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        Log.w("LayerSetupHelper", "Unsupported feature type for $char-$type: ${value?.javaClass?.simpleName}")
                    }
                }
            }

            Log.d("LayerSetupHelper", "Finished processing character: $char")
        }

        Log.d("LayerSetupHelper", "=== Finished applyFeaturesToLayers ===")
    }

    /** Update a feature for a character using an asset path (stores string in outfitJson) */
    fun updateFeatureFromAsset(character: String, type: String, assetPath: String) {
        initLayerManager()
        val layerKey = "$character-$type"
        if (layerManager.hasLayer(layerKey)) layerManager.removeLayer(layerKey)

        val charFeatures = outfitJson.optJSONObject(character) ?: JSONObject().also {
            outfitJson.put(character, it)
        }
        charFeatures.put(type, assetPath)

        val config = characterConfigs[character] ?: CharacterConfig(0, emptyMap(), 0f)
        val bodyView = layerManager.getLayersForCharacter(character)
            .find { layerManager.getKeyForLayerView(it) == "$character-body" }

        val offsetX = bodyView?.translationX ?: config.offsetX
        val offsetY = bodyView?.translationY ?: config.offsetY

        layerManager.setLayerFromAsset(
            layerKey,
            assetPath,
            scaleX = config.scale,
            scaleY = config.scale,
            offsetX = offsetX,
            offsetY = offsetY
        )
        Log.d("LayerSetupHelper", "Updated asset feature: $character-$type -> $assetPath")
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
        // Merge incoming outfit JSON with defaults loaded from assets so missing features fall back to defaults
        ensureDefaultFeaturesLoaded()
        try {
            val incoming = JSONObject(json)
            Log.d("LayerSetupHelper", "setOutfitJson: incoming JSON length=${incoming.toString().length}")
            mergeOutfitJson(incoming)
        } catch (e: Exception) {
            Log.e("LayerSetupHelper", "setOutfitJson: invalid JSON string received; error=${e.message}")
        }
        applyFeaturesToLayers()
    }

    fun setOutfitJsonWithBackground(json: String, replace: Boolean = false) {
        ensureDefaultFeaturesLoaded()

        // Accept either a JSON string (containing features + backgroundId) or a plain backgroundId string.
        if (json.isBlank()) return

        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            Log.d("LayerSetupHelper", "setOutfitJsonWithBackground: input is not JSON, treating as backgroundId string: '$json'")
            JSONObject().put("backgroundId", json)
        }

        if (replace) outfitJson = JSONObject()
        Log.d("LayerSetupHelper", "setOutfitJsonWithBackground: merging root=$root")
        mergeOutfitJson(root)

        // Handle background which can be Int (resource id) or String (asset path or drawable name)
        val bg = root.opt("backgroundId")
        Log.d("LayerSetupHelper", "setOutfitJsonWithBackground: backgroundId raw value=$bg (type=${bg?.javaClass?.simpleName})")
        when (bg) {
            is Int -> {
                try {
                    photoEditorView.background = ContextCompat.getDrawable(context, bg)
                    outfitJson.put("backgroundId", bg)
                    Log.d("LayerSetupHelper", "Background set from resource id: $bg")
                } catch (e: Exception) {
                    Log.e("LayerSetupHelper", "Failed to set background from resource id=$bg: ${e.message}")
                }
            }
            is String -> {
                val bgStr = bg as String
                // Sometimes the string may actually be an integer in string form
                val maybeInt = bgStr.toIntOrNull()
                if (maybeInt != null) {
                    try {
                        photoEditorView.background = ContextCompat.getDrawable(context, maybeInt)
                        outfitJson.put("backgroundId", maybeInt)
                        Log.d("LayerSetupHelper", "Background string parsed as int and set resource id: $maybeInt")
                    } catch (e: Exception) {
                        Log.e("LayerSetupHelper", "Failed to set background from parsed int id=$maybeInt: ${e.message}")
                    }
                } else {
                    // Try drawable resource lookup by name
                    val packageName = context.packageName
                    val resId = try {
                        context.resources.getIdentifier(bgStr, "drawable", packageName)
                    } catch (t: Throwable) {
                        0
                    }

                    if (resId != 0) {
                        photoEditorView.background = ContextCompat.getDrawable(context, resId)
                        outfitJson.put("backgroundId", resId)
                        Log.d("LayerSetupHelper", "Background set from drawable name '$bgStr' -> resId=$resId")
                    } else {
                        // Finally try asset path
                        try {
                            setBackgroundFromAsset(bgStr)
                            Log.d("LayerSetupHelper", "Background set from asset path: $bgStr")
                        } catch (e: Exception) {
                            Log.e("LayerSetupHelper", "Unable to set background for string '$bgStr': ${e.message}")
                        }
                    }
                }
            }
            null -> {
                Log.d("LayerSetupHelper", "No backgroundId present in root JSON")
            }
            else -> {
                Log.w("LayerSetupHelper", "Unhandled backgroundId type: ${bg?.javaClass?.simpleName}")
            }
        }

        applyFeaturesToLayers()
    }

    fun getOutfitJson(): String = outfitJson.toString()

    fun getFullOutfitJson(backgroundId: String? = null, backgroundUri: Uri? = null): String {
        val root = JSONObject(getOutfitJson())

        backgroundId?.toIntOrNull()?.let { id ->
            root.put("backgroundId", id)
        }

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
    fun setBackground(assetPath: String) {
        try {
            // Nếu chưa có đuôi, mặc định là .png
            val fixedPath = if (!assetPath.contains('.')) "background/$assetPath.png" else "background/$assetPath"

            Log.d("LayerSetupHelper", "🔹 Loading background from asset: $fixedPath")

            // Đảm bảo layout đã đo xong
            photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    try {
                        context.assets.open(fixedPath).use { inputStream ->
                            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                            if (bitmap != null) {
                                initLayerManager()
                                // Thêm background layer đầu tiên, full container
                                layerManager.setLayer(
                                    "background",
                                    bitmap,
                                    widthRatio = 1f,
                                    heightRatio = 1f,
                                    scaleX = 1f,
                                    scaleY = 1f,
                                    offsetX = 0f,
                                    offsetY = 0f,
                                    index = 0 // luôn đứng dưới cùng layer khác
                                )
                                Log.d("LayerSetupHelper", "✅ Background loaded successfully from: $fixedPath")
                            } else {
                                Log.e("LayerSetupHelper", "❌ Failed to decode bitmap from: $fixedPath")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LayerSetupHelper", "❌ Exception loading background '$assetPath': ${e.message}")
                        e.printStackTrace()
                    }
                }
            })

        } catch (e: Exception) {
            Log.e("LayerSetupHelper", "❌ Exception preparing background '$assetPath': ${e.message}")
            e.printStackTrace()
        }
    }


    fun resetToInitialState() {
        initLayerManager()
        layerManager.clearLayers(keepBase = false)

        // ensure default features are loaded from JSON before resetting
        ensureDefaultFeaturesLoaded()

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
            // use defaults from outfitJson if present
            val defaults = outfitJson.optJSONObject(char) ?: JSONObject()
            outfitJson.put(char, defaults)
        }
        applyFeaturesToLayers()
    }
}

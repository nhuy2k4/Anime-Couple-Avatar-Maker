package com.app.base.core.layer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

class LayerManager(private val container: FrameLayout, private val context: Context) {

    private val layers = mutableMapOf<String, ImageView>()       // key -> ImageView
    private val layerKeys = mutableMapOf<ImageView, String>()    // ImageView -> key
    private val layerBitmaps = mutableMapOf<String, Bitmap>()    // key -> Bitmap
    private val layerResIds = mutableMapOf<String, Int>()        // key -> resId

    // Mapping for feature follow body
    private val layerFollowBody = mutableMapOf<String, String?>() // key -> bodyKey

    fun setLayer(
        key: String,
        resId: Int,
        widthRatio: Float = 1f,
        heightRatio: Float = 1f,
        leftMarginRatio: Float = 0f,
        topMarginRatio: Float = 0f,
        rightMarginRatio: Float = 0f,
        gravity: Int = Gravity.TOP or Gravity.START,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        followBody: String? = null  // <-- layer sẽ bám theo body
    ) {
        Log.d("LayerManager", "Before setLayer $key, children=${container.childCount}")

        val oldResId = layerResIds[key]
        if (oldResId == resId) return

        layers[key]?.let {
            container.removeView(it)
            layerKeys.remove(it)
            layers.remove(key)
            layerBitmaps.remove(key)
            layerResIds.remove(key)
            layerFollowBody.remove(key)
        }

        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        layerBitmaps[key] = bitmap
        layerResIds[key] = resId
        layerFollowBody[key] = followBody

        val containerWidth = container.width
        val containerHeight = container.height
        val viewWidth = if (leftMarginRatio > 0f && rightMarginRatio > 0f)
            (containerWidth * (1f - leftMarginRatio - rightMarginRatio)).toInt()
        else (containerWidth * widthRatio).toInt()
        val viewHeight = (containerHeight * heightRatio).toInt()

        val finalOffsetX = followBody?.let { bodyKey ->
            layers[bodyKey]?.translationX ?: offsetX
        } ?: offsetX

        val finalOffsetY = followBody?.let { bodyKey ->
            layers[bodyKey]?.translationY ?: offsetY
        } ?: offsetY

        val view = ImageView(context).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight).apply {
                this.gravity = gravity
                topMargin = (containerHeight * topMarginRatio).toInt()
                leftMargin = (containerWidth * leftMarginRatio).toInt()
                rightMargin = (containerWidth * rightMarginRatio).toInt()
            }
            this.scaleX = scaleX
            this.scaleY = scaleY
            this.translationX = finalOffsetX
            this.translationY = finalOffsetY
        }

        container.addView(view)
        layers[key] = view
        layerKeys[view] = key

        Log.d("LayerManager", "After setLayer $key, children=${container.childCount}")
    }

    // Overloaded setLayer method to accept a Bitmap directly
    fun setLayer(
        key: String,
        bitmap: Bitmap,
        widthRatio: Float = 1f,
        heightRatio: Float = 1f,
        leftMarginRatio: Float = 0f,
        topMarginRatio: Float = 0f,
        rightMarginRatio: Float = 0f,
        gravity: Int = Gravity.TOP or Gravity.START,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f,
        followBody: String? = null,
        index: Int? = null // thêm index tùy chọn
    ) {
        Log.d("LayerManager", "Before setLayer $key, children=${container.childCount}")

        layers[key]?.let {
            container.removeView(it)
            layerKeys.remove(it)
            layers.remove(key)
            layerBitmaps.remove(key)
            layerResIds.remove(key)
            layerFollowBody.remove(key)
        }

        layerBitmaps[key] = bitmap
        layerFollowBody[key] = followBody

        val containerWidth = container.width
        val containerHeight = container.height
        val viewWidth = if (leftMarginRatio > 0f && rightMarginRatio > 0f)
            (containerWidth * (1f - leftMarginRatio - rightMarginRatio)).toInt()
        else (containerWidth * widthRatio).toInt()
        val viewHeight = (containerHeight * heightRatio).toInt()

        val finalOffsetX = followBody?.let { bodyKey ->
            layers[bodyKey]?.translationX ?: offsetX
        } ?: offsetX

        val finalOffsetY = followBody?.let { bodyKey ->
            layers[bodyKey]?.translationY ?: offsetY
        } ?: offsetY

        val view = ImageView(context).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight).apply {
                this.gravity = gravity
                topMargin = (containerHeight * topMarginRatio).toInt()
                leftMargin = (containerWidth * leftMarginRatio).toInt()
                rightMargin = (containerWidth * rightMarginRatio).toInt()
            }
            this.scaleX = scaleX
            this.scaleY = scaleY
            this.translationX = finalOffsetX
            this.translationY = finalOffsetY
        }
        if (index != null) {
            container.addView(view, index)
        } else {
            container.addView(view)
        }

        layers[key] = view
        layerKeys[view] = key

        Log.d("LayerManager", "After setLayer $key, children=${container.childCount}")
    }

    // Set layer using an image stored in assets (path relative to assets/)
    fun setLayerFromAsset(
        key: String,
        assetPath: String,
        widthRatio: Float = 1f,
        heightRatio: Float = 1f,
        leftMarginRatio: Float = 0f,
        topMarginRatio: Float = 0f,
        rightMarginRatio: Float = 0f,
        gravity: Int = Gravity.TOP or Gravity.START,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        try {
            Log.d("LayerManager", "Attempting to load asset: $assetPath for key: $key")

            // Check if the file exists in the assets directory
            val assetManager = context.assets
            val files = try {
                assetManager.list(assetPath.substringBeforeLast('/'))?.toList() ?: emptyList()
            } catch (e: Exception) {
                Log.e("LayerManager", "Cannot list directory for $assetPath: ${e.message}")
                emptyList()
            }

            Log.d("LayerManager", "Available files in directory: $files")
            Log.d("LayerManager", "Looking for file: ${assetPath.substringAfterLast('/')}")

            val inputStream = assetManager.open(assetPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            if (bitmap != null) {
                Log.d("LayerManager", "Successfully loaded asset: $assetPath")
                setLayer(key, bitmap, widthRatio, heightRatio, leftMarginRatio, topMarginRatio, rightMarginRatio, gravity, scaleX, scaleY, offsetX, offsetY)
            } else {
                Log.e("LayerManager", "Failed to create bitmap from asset: $assetPath")
            }
            inputStream.close()
        } catch (e: Exception) {
            Log.e("LayerManager", "Exception loading asset $assetPath: ${e.message}")
            e.printStackTrace()

// ✅ Try smart alternative paths based on feature folder structure
            val baseName = assetPath.substringAfterLast('/')
            val folderName = when {
                baseName.startsWith("frontHair") -> "frontHair"
                baseName.startsWith("backHair") -> "backHair"
                baseName.startsWith("eyes") -> "eyes"
                baseName.startsWith("shirt") -> "shirt"
                baseName.startsWith("pants") -> "pants"
                baseName.startsWith("dress") -> "dress"
                baseName.startsWith("shoes") -> "shoes"
                baseName.startsWith("hat") -> "hat"
                baseName.startsWith("accessory") -> "accessory"
                else -> ""
            }

            val alternativePaths = buildList {
                add("features/$folderName/$baseName.png")
                add("features/$folderName/$baseName")
            }


            for (altPath in alternativePaths) {
                try {
                    Log.d("LayerManager", "Trying alternative path: $altPath")
                    val inputStream = context.assets.open(altPath)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        Log.d("LayerManager", "Success with alternative path: $altPath")
                        setLayer(key, bitmap, widthRatio, heightRatio, leftMarginRatio, topMarginRatio, rightMarginRatio, gravity, scaleX, scaleY, offsetX, offsetY)
                        inputStream.close()
                        return
                    }
                    inputStream.close()
                } catch (ex: Exception) {
                    Log.d("LayerManager", "Alternative path $altPath also failed: ${ex.message}")
                }
            }

            Log.e("LayerManager", "All paths failed for asset: $assetPath")
        }
    }

    fun getKeyForLayerView(view: ImageView): String? = layerKeys[view]

    fun hasLayer(key: String): Boolean = layers.containsKey(key)

    fun removeLayer(key: String) {
        layers[key]?.let {
            container.removeView(it)
            layerKeys.remove(it)
            layerBitmaps.remove(key)
            layerResIds.remove(key)
            layerFollowBody.remove(key)
            layers.remove(key)
        }
    }

    fun clearLayers(keepBase: Boolean = true) {
        val keys = layers.keys.toList()
        for (key in keys) {
            if (keepBase && key.endsWith("-body")) continue
            removeLayer(key)
        }
    }

    fun getLayersForCharacter(character: String): List<ImageView> =
        layers.filterKeys { it.startsWith("$character-") }.values.toList()

    fun getLayerBitmap(key: String): Bitmap? = layerBitmaps[key]

    fun applyOutfit(outfit: Map<String, Int>) {
        outfit.forEach { (key, resId) -> setLayer(key, resId) }
    }
}

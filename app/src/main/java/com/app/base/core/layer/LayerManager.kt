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

    fun setLayer(
        key: String,
        resId: Int,
        widthRatio: Float = 1f,
        heightRatio: Float = 1f,
        leftMarginRatio: Float = 0f,
        topMarginRatio: Float = 0f,
        rightMarginRatio: Float = 0f,
        gravity: Int = Gravity.TOP or Gravity.START
    ) {
        Log.d("LayerManager", "Before setLayer $key, children=${container.childCount}")

        // Nếu resId giống layer hiện tại -> skip
        val oldResId = layerResIds[key]
        if (oldResId == resId) {
            Log.d("LayerManager", "Layer $key already has same resId, skip update")
            return
        }

        // Remove layer cũ nếu có
        layers[key]?.let {
            container.removeView(it)
            layerKeys.remove(it)
            layers.remove(key)
            layerBitmaps.remove(key)
            layerResIds.remove(key)
            Log.d("LayerManager", "Removed layer: $key, children=${container.childCount}")
        }

        // Decode bitmap mới và lưu cache
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        layerBitmaps[key] = bitmap
        layerResIds[key] = resId

        val containerWidth = container.width
        val containerHeight = container.height
        val viewWidth = if (leftMarginRatio > 0f && rightMarginRatio > 0f)
            (containerWidth * (1f - leftMarginRatio - rightMarginRatio)).toInt()
        else (containerWidth * widthRatio).toInt()
        val viewHeight = (containerHeight * heightRatio).toInt()

        val view = ImageView(context).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight).apply {
                this.gravity = gravity
                topMargin = (containerHeight * topMarginRatio).toInt()
                leftMargin = (containerWidth * leftMarginRatio).toInt()
                rightMargin = (containerWidth * rightMarginRatio).toInt()
            }
        }

        container.addView(view)
        layers[key] = view
        layerKeys[view] = key

        Log.d("LayerManager", "After setLayer $key, children=${container.childCount}")
    }

    fun getKeyForLayerView(view: ImageView): String? = layerKeys[view]

    fun hasLayer(key: String): Boolean = layers.containsKey(key)

    fun removeLayer(key: String) {
        layers[key]?.let {
            container.removeView(it)
            layerKeys.remove(it)
            layerBitmaps.remove(key)
            layerResIds.remove(key)
            layers.remove(key)
            Log.d("LayerManager", "Removed layer: $key, children=${container.childCount}")
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

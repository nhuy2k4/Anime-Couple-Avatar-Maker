package com.app.base.ui.category

import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.view.Gravity

class LayerManager(private val container: FrameLayout, private val context: Context) {

    // Map lưu từng layer theo tên (type) như "hair", "eyes", "nose", "shirt"…
    private val layers = mutableMapOf<String, ImageView>()

    /**
     * Thêm hoặc thay thế 1 layer
     * @param type: loại layer (hair, eyes, nose, mouth…)
     * @param resId: resource drawable
     * @param widthRatio: chiều ngang layer so với container
     * @param heightRatio: chiều cao layer so với container
     * @param topMarginRatio: khoảng cách từ trên xuống so với container
     * @param gravity: vị trí layer
     */
    fun setLayer(
        type: String,
        resId: Int,
        widthRatio: Float = 1f,
        heightRatio: Float = 1f,
        topMarginRatio: Float = 0f,
        gravity: Int = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    ) {
        // Remove layer cũ nếu đã có
        layers[type]?.let { container.removeView(it) }

        val containerWidth = container.width
        val containerHeight = container.height

        val view = ImageView(context).apply {
            setImageResource(resId)
            layoutParams = FrameLayout.LayoutParams(
                (containerWidth * widthRatio).toInt(),
                (containerHeight * heightRatio).toInt()
            ).apply {
                this.gravity = gravity
                topMargin = (containerHeight * topMarginRatio).toInt()
            }
        }

        container.addView(view)
        layers[type] = view
    }

    /**
     * Remove layer theo type
     */
    fun removeLayer(type: String) {
        layers[type]?.let { container.removeView(it) }
        layers.remove(type)
    }

    /**
     * Remove tất cả layer trừ base
     */
    fun clearLayers(keepBase: Boolean = true) {
        val keys = layers.keys.toList()
        for (key in keys) {
            if (keepBase && key == "base") continue
            removeLayer(key)
        }
    }
}

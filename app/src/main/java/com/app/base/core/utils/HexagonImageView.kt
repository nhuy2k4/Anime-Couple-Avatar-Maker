package com.app.base.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class HexagonImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val path = Path()
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val r = w / 2f
        val triangleHeight = (Math.sqrt(3.0) * r / 2).toFloat()
        val cx = w / 2f
        val cy = h / 2f

        path.reset()
        path.moveTo(cx, cy - r) // top
        path.lineTo(cx + triangleHeight, cy - r / 2)
        path.lineTo(cx + triangleHeight, cy + r / 2)
        path.lineTo(cx, cy + r)
        path.lineTo(cx - triangleHeight, cy + r / 2)
        path.lineTo(cx - triangleHeight, cy - r / 2)
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.onDraw(canvas)
        canvas.restoreToCount(save)
    }
}

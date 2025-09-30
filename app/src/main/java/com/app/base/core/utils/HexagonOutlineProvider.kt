package com.app.base.core.utils

import android.graphics.Outline
import android.graphics.Path
import android.view.View
import android.view.ViewOutlineProvider

class HexagonOutlineProvider : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        val w = view.width.toFloat()
        val h = view.height.toFloat()
        val path = Path()

        val midX = w / 2
        val quarterX = w / 4
        val threeQuarterX = 3 * w / 4
        val midY = h / 2

        // 6 đỉnh lục giác
        path.moveTo(midX, 0f)
        path.lineTo(w, midY / 2)
        path.lineTo(w, midY + midY / 2)
        path.lineTo(midX, h)
        path.lineTo(0f, midY + midY / 2)
        path.lineTo(0f, midY / 2)
        path.close()

        outline.setConvexPath(path)
    }
}

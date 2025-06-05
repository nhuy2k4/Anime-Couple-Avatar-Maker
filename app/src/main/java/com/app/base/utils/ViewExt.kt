package com.app.base.utils

import android.graphics.LinearGradient
import android.graphics.Shader
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.brally.mobile.base.R
import com.brally.mobile.base.application.getBaseApplication

fun AppCompatTextView.setGradient(
    listColor: IntArray = intArrayOf(
        R.color.pink_fd8aff,
        R.color.blue_00b2ff
    )
) {
    post {
        try {
            this.text = this.text
            val width = paint.measureText(this.text.toString())
            val listParseColor = listColor.map {
                ContextCompat.getColor(getBaseApplication(), it)
            }.toIntArray()
            val shader = LinearGradient(
                0f, 0f,
                width, this.textSize,
                listParseColor, null,
                Shader.TileMode.CLAMP
            )

            this.paint.shader = shader
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

package com.app.base.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.ImageView
import com.app.base.ui.main.MainViewModel

class PhotoSaveHelper(
    private val container: View,
    private val photoEditorView: ImageView,
    private val context: Context
) {

    private var tempBackgroundResId: Int? = null
    private var tempOutfitJson: String = "{}"

    /**
     * Setup initial layers (call khi onViewCreated)
     */
    fun setupInitialLayers(init: () -> Unit) {
        init()
    }

    /** Chỉ cập nhật background tạm thời */
    fun updateTemporaryBackground(resId: Int) {
        tempBackgroundResId = resId
        photoEditorView.setImageResource(resId)
    }

    /** Lấy outfit hiện tại + background tạm thời */
    fun getOutfitJsonWithBackground(defaultBackground: Int): String {
        val bgRes = tempBackgroundResId ?: defaultBackground
        // Tạm giả sử đây là merge JSON + background
        return mergeJsonWithBackground(tempOutfitJson, bgRes)
    }

    /** Khi commit thật: lấy bitmap */
    fun captureBitmap(): Bitmap {
        val width = container.width
        val height = container.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        container.draw(canvas)
        return bitmap
    }

    /** Update temporary outfit JSON */
    fun setTemporaryOutfitJson(json: String) {
        tempOutfitJson = json
    }

    private fun mergeJsonWithBackground(json: String, backgroundResId: Int): String {
        // Đây là placeholder, merge JSON với background
        return json.replace("\"background\":\\d+".toRegex(), "\"background\":$backgroundResId")
    }

}

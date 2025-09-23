package com.app.base.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.View
import com.app.base.ui.main.MainViewModel
import java.io.File

object PhotoCommitHelper {

    /**
     * Commit container thành bitmap, lưu vào ViewModel và JSON vào prefs
     */
    fun commitFromContainer(
        container: View,
        outfitJson: String,
        mainViewModel: MainViewModel,
        backgroundUri: Uri? = null,
        context: Context? = null,  // thêm context để lưu JSON
        onDone: (() -> Unit)? = null
    ) {
        container.post {
            val width = container.width
            val height = container.height
            if (width == 0 || height == 0) return@post

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            container.draw(canvas)

            if (backgroundUri != null) {
                // cập nhật outfit hiện tại thay vì tạo mới
                mainViewModel.updateCurrentOutfit(outfitJson, uri = backgroundUri)
            } else {
                mainViewModel.applyAndSaveOutfit(outfitJson, bitmap)
            }

            // Lưu JSON ra file / SharedPreferences để giữ lại khi thoát app
            if (backgroundUri != null && context != null) {
                saveOutfitJson(backgroundUri, outfitJson, context)
            }

            onDone?.invoke()
        }
    }

    /**
     * Xóa các outfit tạm, giữ lại current outfit
     */
    fun clearTemporaryOutfits(mainViewModel: MainViewModel) {
        val current = mainViewModel.currentOutfit.value
        mainViewModel._outfitListData.value = if (current != null) listOf(current) else emptyList()
    }

    /**
     * Lưu outfit JSON liên quan đến URI
     * @param uri URI của ảnh
     * @param outfitJson JSON của outfit
     */
    /**
     * Commit và save tạm thời
     */
    fun saveAndCommit(
        container: View,
        outfitJson: String,
        mainViewModel: MainViewModel,
        backgroundUri: Uri? = null,
        context: Context? = null,
        onDone: (() -> Unit)? = null
    ) {
        commitFromContainer(container, outfitJson, mainViewModel, backgroundUri, context, onDone)
    }

    /**
     * Load outfit JSON từ URI
     */
    fun saveOutfitJson(uri: Uri, outfitJson: String, context: Context) {
        val prefs = context.getSharedPreferences("outfit_map", Context.MODE_PRIVATE)
        prefs.edit().putString(uri.toString(), outfitJson).apply()
    }

    fun loadOutfitJson(uri: Uri, context: Context): String? {
        val prefs = context.getSharedPreferences("outfit_map", Context.MODE_PRIVATE)
        return prefs.getString(uri.toString(), null)
    }

}

package com.app.base.core.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.View
import com.app.base.R
import com.app.base.ui.main.MainViewModel
import java.io.File
import org.json.JSONObject

object PhotoSaver {

    fun savePhoto(
        container: View,
        outfitJson: String,
        mainViewModel: MainViewModel,
        onSuccess: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        container.post {
            val width = container.width
            val height = container.height
            if (width == 0 || height == 0) return@post

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            container.draw(canvas)

            mainViewModel.saveBitmapToGallery(bitmap, outfitJson) { uri, mediaId ->
                if (uri != null) {
                    Log.d("PhotoSaver", "Bitmap saved at: $uri, mediaId=$mediaId")
                    Log.d("PhotoSaver", "Outfit JSON saved: $outfitJson")
                    onSuccess?.invoke()
                } else {
                    Log.e("PhotoSaver", "Error saving bitmap")
                    onError?.invoke()
                }
            }
        }
    }

    fun saveOutfitJsonAlongWithPhoto(photoFile: File, outfitJson: String) {
        try {
            val jsonFile = File(photoFile.parentFile, "${photoFile.nameWithoutExtension}.json")
            jsonFile.writeText(outfitJson)
            Log.d("PhotoSaver", "Saved outfit JSON to ${jsonFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

package com.app.base.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView

object AssetImageLoader {
    fun loadImage(imageView: ImageView, context: Context, assetPath: String) {
        try {
            context.assets.open(assetPath).use { inputStream ->

                Log.d("AssetLoader", "Loading asset: $assetPath")

                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // fallback nếu không load được
            imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }
    }
}

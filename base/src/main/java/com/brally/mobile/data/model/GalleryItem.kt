package com.brally.mobile.data.model

import android.net.Uri

data class GalleryItem(
    val imageUri: Uri,
    val outfitJson: String? = null  // giữ để tương thích (nullable)
)

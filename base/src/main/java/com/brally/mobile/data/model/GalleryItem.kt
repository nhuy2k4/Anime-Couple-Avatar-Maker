package com.brally.mobile.data.model

import android.net.Uri

data class GalleryItem(
    val imageUri: Uri,
    val outfitJson: String? = null  // cho phép null vì có ảnh không kèm json
)

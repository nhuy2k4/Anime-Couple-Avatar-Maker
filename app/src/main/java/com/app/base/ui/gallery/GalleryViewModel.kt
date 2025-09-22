package com.app.base.ui.gallery

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.viewModelScope
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.GalleryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel : BaseViewModel() {

    private val _photos = MutableStateFlow<List<GalleryItem>>(emptyList())
    val photos: StateFlow<List<GalleryItem>> = _photos

    fun loadSavedPhotos(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val photoList = mutableListOf<GalleryItem>()

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val query = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    // Tạo GalleryItem (ở đây chưa có json -> để null)
                    photoList.add(GalleryItem(contentUri, null))
                }
            }

            _photos.value = photoList
        }
    }
}

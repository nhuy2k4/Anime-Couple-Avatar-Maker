package com.app.base.ui.gallery

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.GalleryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.app.base.ui.main.MainViewModel
class GalleryViewModel : BaseViewModel() {

    private val _photos = MutableStateFlow<List<GalleryItem>>(emptyList())
    val photos: StateFlow<List<GalleryItem>> = _photos

    fun loadSavedPhotos(context: Context, mainViewModel: MainViewModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val photoList = mutableListOf<GalleryItem>()
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val query = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
            )

            query?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )

                    // Lookup outfit by URI instead of mediaId
                    val outfitData = mainViewModel.getOutfitDataByUri(contentUri)
                    val prefs = context.getSharedPreferences("outfit_map", Context.MODE_PRIVATE)
                    val savedJson = prefs.getString(contentUri.toString(), null)
                    val json = outfitData?.json ?: savedJson

                    photoList.add(GalleryItem(contentUri, json))
                    Log.d("GalleryVM", "Loaded photo uri=$contentUri, outfitJson=$json")
                }
            }

            _photos.value = photoList
        }
    }


}


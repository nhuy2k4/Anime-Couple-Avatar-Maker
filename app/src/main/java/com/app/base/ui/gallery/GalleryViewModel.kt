package com.app.base.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brally.mobile.data.model.GalleryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel : ViewModel() {

    // Flow chứa danh sách ảnh
    private val _photos = MutableStateFlow<List<GalleryItem>>(emptyList())
    val photos: StateFlow<List<GalleryItem>> = _photos

    // Load dữ liệu giả ban đầu
    fun loadInitialData() {
        viewModelScope.launch {
            val dummyPhotos = listOf(
                GalleryItem(R.drawable.photo1),
                GalleryItem(R.drawable.photo2),
                GalleryItem(R.drawable.photo3),
                GalleryItem(R.drawable.photo4),
                GalleryItem(R.drawable.photo5),
                GalleryItem(R.drawable.photo6)
            )
            _photos.value = dummyPhotos
        }
    }
}

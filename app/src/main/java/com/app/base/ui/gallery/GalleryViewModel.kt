package com.app.base.ui.gallery

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.app.base.database.AppDatabase
import com.app.base.database.OutfitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryViewModel : BaseViewModel() {

    private val _photos = MutableStateFlow<List<OutfitEntity>>(emptyList())
    val photos: StateFlow<List<OutfitEntity>> = _photos

    /** Load tất cả outfit từ Room DB */
    fun loadSavedOutfits(context: Context) {
        viewModelScope.launch {
            val outfits = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(context).outfitDao().getAllOutfits()
            }
            _photos.value = outfits
        }
    }

    /** Xóa outfit và cập nhật state Flow */
    fun deletePhoto(context: Context, outfit: OutfitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                AppDatabase.getInstance(context).outfitDao().deleteOutfit(outfit.id)
                // Cập nhật list sau khi xóa
                val updatedList = _photos.value.filter { it.id != outfit.id }
                _photos.value = updatedList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

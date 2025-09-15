package com.app.base.ui.photograph

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.app.base.R
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.PhotographItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel quản lý dữ liệu cho PhotographFragment
 * - Quản lý danh sách ảnh sample từ drawable
 * - Có thể mở rộng thêm load từ local storage hoặc remote server
 */
class PhotographViewModel : BaseViewModel() {

    // ========== STATE FLOWS ==========
    private val _photos = MutableStateFlow<List<PhotographItem>>(emptyList())
    val photos = _photos.asStateFlow()

    private val _selectedPhotoIndex = MutableStateFlow(0)
    val selectedPhotoIndex = _selectedPhotoIndex.asStateFlow()

    private var isDataLoading = false

    // ========== LIFECYCLE ==========
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadInitialData()
    }

    // ========== PUBLIC METHODS ==========
    fun loadInitialData() {
        if (isDataLoading) return
        loadLocalPhotos()
    }

    fun selectPhoto(position: Int) {
        if (position in _photos.value.indices) {
            _selectedPhotoIndex.value = position
        }
    }

    fun refreshData() {
        isDataLoading = false
        loadInitialData()
    }

    // ========== PRIVATE ==========
    private fun loadLocalPhotos() {
        isDataLoading = true
        launchHandler {
            flowOnIO {
                listOf(
                    PhotographItem("Photo 1", R.drawable.photo1),
                    PhotographItem("Photo 2", R.drawable.photo2)
                )
            }.subscribe { list ->
                _photos.value = list
                isDataLoading = false
            }
        }
    }

}

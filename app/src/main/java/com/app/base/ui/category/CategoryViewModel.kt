package com.app.base.ui.category

import androidx.lifecycle.LifecycleOwner
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.language_onboard.data.local.CommonAppSharePref
import com.language_onboard.data.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.app.base.R

/**
 * ViewModel quản lý dữ liệu cho CategoryFragment
 * - Categories lấy local (drawable icons)
 * - Arts lấy từ server (AppRemoteConfig)
 * - Lọc arts theo CategoryItem.type
 */
class CategoryViewModel : BaseViewModel() {

    // ========== STATE FLOWS ==========
    private val _listArt = MutableStateFlow<List<ArtItem>>(emptyList())
    val listArt = _listArt.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _categorySelected = MutableStateFlow(0)
    val categorySelected = _categorySelected.asStateFlow()

    // ========== PRIVATE ==========
    private var allArt = mutableListOf<ArtItem>()
    private var isDataLoading = false

    // ========== LIFECYCLE ==========
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadInitialData()
    }

    // ========== PUBLIC METHODS ==========
    fun loadInitialData() {
        if (isDataLoading) return
        loadLocalCategories()
        loadArtsByCategory(_categorySelected.value)
    }

    /** Dùng categories local (drawable icons) */
    private fun loadLocalCategories() {
        val localCategories = listOf(
            CategoryItem(type = "eye", value = "Eye", iconResId = R.drawable.eye),
            CategoryItem(type = "hair", value = "Hair", iconResId = R.drawable.hair)
            // Thêm nếu cần: skin, eyebrow, mouth
        )
        _categories.value = localCategories
    }


    fun getArtsByCategory(position: Int) {
        if (position >= _categories.value.size) return

        val category = _categories.value[position]
        _categorySelected.value = position

        val filteredArts = if (category.type.equals("all", ignoreCase = true)) {
            allArt
        } else {
            allArt.filter { it.category.equals(category.type, ignoreCase = true) }
                .sortedBy { it.order }
        }
        _listArt.value = filteredArts
    }

    fun getCategoryPosition(categoryItem: CategoryItem): Int {
        return _categories.value.indexOf(categoryItem)
    }

    fun refreshData() {
        isDataLoading = false
        loadInitialData()
    }

    // ========== PRIVATE ==========
    private fun loadArtsByCategory(position: Int) {
        isDataLoading = true
        launchHandler {
            flowOnIO {
                AppRemoteConfig.getListArt() // vẫn lấy art từ server
            }.subscribe { arts ->
                allArt = arts.toMutableList()
                getArtsByCategory(position)
                isDataLoading = false
            }
        }
    }
}
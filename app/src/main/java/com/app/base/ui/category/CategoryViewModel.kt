package com.app.base.ui.category

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.app.base.database.AppDatabase
import com.app.base.database.entity.OutfitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class CategoryViewModel : BaseViewModel() {

    // ========== STATE FLOWS ==========
    private val _listArt = MutableStateFlow<List<ArtItem>>(emptyList())
    val listArt = _listArt.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _categorySelected = MutableStateFlow(0)
    val categorySelected = _categorySelected.asStateFlow()

    private val _currentOutfit = MutableStateFlow<OutfitEntity?>(null)
    val currentOutfit = _currentOutfit.asStateFlow()

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

    /** Load outfit mặc định từ RoomDB */
    suspend fun loadDefaultOutfit(context: Context) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val dao = db.outfitDao()

            // Lấy outfit đầu tiên trong DB nếu có
            val defaultOutfit = dao.getAllOutfits().firstOrNull() ?: run {
                // Nếu DB trống, chỉ tạo mặc định trong code
                OutfitEntity(
                    features = mapOf(
                        "male.hair" to "features/frontHair/frontHair_m_3.png",
                        "male.eye" to "features/eyes/eyes_m_3.png",
                        "female.hair" to "features/frontHair/frontHair_f_1.png",
                        "female.eye" to "features/eyes/eyes_f_2.png"
                    ),
                    backgroundId = null
                )
            }

            _currentOutfit.value = defaultOutfit
        }
    }


    /** Dùng categories local (drawable icons) */
    private fun loadLocalCategories() {
        val localCategories = listOf(
            CategoryItem(type = "eye", value = "Eye", iconResId = com.app.base.R.drawable.eye),
            CategoryItem(type = "hair", value = "Hair", iconResId = com.app.base.R.drawable.hair)
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

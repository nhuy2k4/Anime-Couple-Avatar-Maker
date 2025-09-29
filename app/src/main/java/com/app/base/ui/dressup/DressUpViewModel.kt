package com.app.base.ui.dressup

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.brally.mobile.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.app.base.R // import drawable resources
import com.app.base.model.FeatureItem
import com.brally.mobile.data.model.TabItem

/**
 * ViewModel quản lý dữ liệu cho DressUpFragment
 * - Tabs lấy drawable (local)
 * - Features/artifacts có thể load từ server hoặc JSON
 */
class DressUpViewModel : BaseViewModel() {

    // ========== STATE FLOWS ==========
    private val _tabs = MutableStateFlow<List<TabItem>>(emptyList())
    val tabs = _tabs.asStateFlow()

    private val _tabSelected = MutableStateFlow(0)
    val tabSelected = _tabSelected.asStateFlow()

    private val _features = MutableStateFlow<List<FeatureItem>>(emptyList())
    val features = _features.asStateFlow()

    private var allFeatures = mutableListOf<FeatureItem>()

    private var isDataLoading = false


    // ========== LIFECYCLE ==========
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadInitialData(context)
    }

    // ========== PUBLIC METHODS ==========
    fun loadInitialData(context: Context) {
        if (isDataLoading) return
        loadLocalTabs()

        val featureList = loadFeaturesFromAssets(context, "features_dev.json")
        allFeatures.clear()
        allFeatures.addAll(featureList)
        _features.value = featureList

        Log.d("DressUpViewModel", "Parsed features count = ${featureList.size}")

        loadFeaturesByTab(_tabSelected.value)
    }



    /** Dùng tabs drawable local */
    private fun loadLocalTabs() {
        val localTabs = listOf(
            TabItem("eyes", "Eyes", R.drawable.tab_eyes),
            TabItem("frontHair", "Hair", R.drawable.tab_hair)
            // TODO: thêm các tab khác nếu cần
        )
        _tabs.value = localTabs
    }
    private fun loadFeaturesFromAssets(context: android.content.Context, fileName: String): List<FeatureItem> {
        return try {
            val jsonStr = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : com.google.gson.reflect.TypeToken<List<FeatureItem>>() {}.type
            com.google.gson.Gson().fromJson(jsonStr, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getFeaturesByTab(position: Int) {
        if (position >= _tabs.value.size) return
        _tabSelected.value = position

        val selectedTab = _tabs.value[position]
        val filteredFeatures = if (selectedTab.id == "all") {
            allFeatures
        } else {
            // TODO: lọc features theo selectedTab.id (category)
            allFeatures
        }
        _features.value = filteredFeatures
    }

    fun getTabPosition(tab: TabItem): Int {
        return _tabs.value.indexOf(tab)
    }

    fun refreshData() {
        isDataLoading = false
        loadInitialData(context)
    }

    // ========== PRIVATE ==========
    fun loadFeaturesByTab(position: Int, gender: String? = null) {
        val tab = _tabs.value.getOrNull(position) ?: return
        val tabId = tab.id

        val filtered = allFeatures.filter {
            it.category == tabId && (it.gender == gender || it.gender == "all")
        }
        _features.value = filtered
        // Thông báo UI cập nhật
    }



}

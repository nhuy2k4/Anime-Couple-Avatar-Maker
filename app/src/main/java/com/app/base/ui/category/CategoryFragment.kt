package com.app.base.ui.category

import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.FragmentCategoryBinding
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.data.model.AdStyle
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.ItemListAds
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.AdManager.showFull
import com.brally.mobile.utils.RecyclerViewType
import com.brally.mobile.utils.collectLatestFlow
import com.brally.mobile.utils.singleClick
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args by navArgs<CategoryFragmentArgs>()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val artAdapter by lazy { ArtAdapter(this) }
    private val categoryTabAdapter by lazy {
        CategoryTabAdapter(onItemTapped = {
            binding.rcvArts.scrollToPosition(0)
            viewModel.getArtsByCategory(viewModel.getCategoryPosition(it))
        })
    }

    private val categoryId by lazy { args.categoryId }

    override fun initView() {
        adjustInsetsForBottomNavigation(binding.top)
        updateUI()

        // Setup category tabs
        binding.rcvCategoriesTab.adapter = categoryTabAdapter
        setupAdapter()
    }

    override fun initListener() {
        artAdapter.setOnClickItemRecyclerView { artItem, _ ->
            //Todo : Handle art item click
        }

        binding.btnBack.singleClick {
            handleBack()
        }

        onBackPressed {
            handleBack()
        }
    }

    private fun handleBack() {
        showFull(AdManager.FULL_DRAW) {
            popBackStack()
        }
    }

    override fun initData() {
        collectLatestFlow(viewModel.listArt) { arts ->
            var data = arts.map {
                ItemListAds.DataItemListAds(it)
            }
            artAdapter.setAllData(insertAdsWithRowAlign(data, 4).toMutableList())
        }

        collectLatestFlow(viewModel.categories) { categories ->
            // Set up the category tabs with the first category and the rest
            if (categories.isNotEmpty()) {
                categoryTabAdapter.setCategories(categories.toMutableList())
            }
        }

        collectLatestFlow(viewModel.categorySelected) { positionSelected ->
            categoryTabAdapter.selectCategory(positionSelected)
        }
    }

    private fun updateUI() {
        binding.apply {
            rcvCategoriesTab.adapter = categoryTabAdapter
            rcvArts.adapter = artAdapter
        }
    }

    private fun setupAdapter() {
        val layoutManager = GridLayoutManager(context, 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (artAdapter.getItemViewType(position)) {
                        RecyclerViewType.TYPE_AD_FULL.value -> 2 // FULL_ITEMS
                        else -> 1 // Data item & FULL_ONE_ITEM & Placeholder
                    }
                }
            }
        }

        binding.rcvArts.apply {
            this.layoutManager = layoutManager
            adapter = artAdapter

            setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                setMaxRecycledViews(RecyclerViewType.TYPE_AD_FULL.value, 2)
                setMaxRecycledViews(RecyclerViewType.TYPE_AD_ONE.value, 4)
            })
        }

    }

    fun insertAdsWithRowAlign(
        original: List<ItemListAds<ArtItem>>, interval: Int
    ): List<ItemListAds<ArtItem>> {
        val result = mutableListOf<ItemListAds<ArtItem>>()
        var columnIndex = 0
        var count = 0

        for (item in original) {
            result.add(item)
            columnIndex = (columnIndex + 1) % 2
            count++

            if (count % interval == 0) {
                if (columnIndex != 0) {
//                    result.add(ItemListAds.Placeholder)
                    columnIndex = 0
                }
                result.add(ItemListAds.Ad(AdStyle.FULL_ITEMS) as ItemListAds<ArtItem>)
                columnIndex = 0
            }
        }
        return result
    }
}

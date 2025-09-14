package com.app.base.ui.category

import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.databinding.FragmentCategoryBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.AdManager.showFull
import com.brally.mobile.utils.collectLatestFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.app.base.ui.main.MainViewModel

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args: CategoryFragmentArgs by navArgs()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val categoryTabAdapter by lazy { CategoryTabAdapter(onItemTapped = ::onCategorySelected) }

    private lateinit var layerHelper: CategoryLayerHelper

    override fun initView() {
        initLayerHelper()
        setupRecyclerViews()
    }

    override fun initListener() {
        setupBackPress()
        setupHomeButton()
        setupResetButton()
        setupSwitchCharacterButton()
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    // ---------------------- Init LayerHelper ----------------------
    private fun initLayerHelper() {
        layerHelper = CategoryLayerHelper(binding)
        layerHelper.setupFeatureAdapter()
        layerHelper.setupInitialLayers()
    }

    // ---------------------- RecyclerView ----------------------
    private fun setupRecyclerViews() {
        binding.rcvCategoriesTab.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryTabAdapter
        }
    }

    // ---------------------- Listeners ----------------------
    private fun setupBackPress() = onBackPressed { handleBackPress() }
    private fun setupHomeButton() = binding.btnHome.setOnClickListener { handleBackPress() }
    private fun setupResetButton() = binding.btnReset.setOnClickListener { layerHelper.resetToInitialState() }
    private fun setupSwitchCharacterButton() = binding.btnChangeMale.setOnClickListener { layerHelper.switchCharacter() }

    // ---------------------- Category Selection ----------------------
    private fun onCategorySelected(category: CategoryItem) {
        scrollArtsToStart()
        loadArtsByCategory()
        layerHelper.showCategoryFeatures(category.type)
    }

    private fun scrollArtsToStart() {
        binding.rcvArts.scrollToPosition(0)
    }

    private fun loadArtsByCategory() {
        viewModel.getArtsByCategory(categoryTabAdapter.getSelectedPosition())
    }

    // ---------------------- Back Press ----------------------
    private fun handleBackPress() {
        showFull(AdManager.FULL_DRAW) { popBackStack() }
    }

    // ---------------------- ViewModel ----------------------
    private fun loadViewModelData() {
        viewModel.loadInitialData()
    }

    private fun observeViewModel() {
        collectLatestFlow(viewModel.categories) { categories ->
            if (categories.isNotEmpty()) categoryTabAdapter.setCategories(categories)
            else categoryTabAdapter.clearCategories()
        }

        collectLatestFlow(viewModel.categorySelected) { pos ->
            categoryTabAdapter.selectCategory(pos)
        }
    }
}

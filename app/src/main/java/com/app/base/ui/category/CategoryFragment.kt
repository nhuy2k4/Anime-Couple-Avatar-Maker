package com.app.base.ui.category

import android.util.Log
import com.app.base.R
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.core.layer.CategoryLayerHelper
import com.app.base.core.layer.LayerSetupHelper
import com.app.base.databinding.FragmentCategoryBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.utils.collectLatestFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.navigate

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args: CategoryFragmentArgs by navArgs()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val categoryTabAdapter by lazy { CategoryTabAdapter(onItemTapped = ::onCategorySelected) }

    // Tách 2 helper
    private lateinit var layerSetupHelper: LayerSetupHelper
    private lateinit var categoryLayerHelper: CategoryLayerHelper

    override fun initView() {
        initHelpers()
        setupRecyclerViews()
        categoryLayerHelper.setupDefaultTab()
    }

    override fun initListener() {
        setupHomeButton()
        setupResetButton()
        setupCameraButton()
        setupSwitchCharacterButton()
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    /**
     * Khởi tạo 2 helper
     */
    private fun initHelpers() {
        // 1. Setup body + layers
        layerSetupHelper = LayerSetupHelper(
            binding.photoContainer,
            binding.photoEditorView,
            requireContext()
        )
        layerSetupHelper.setupInitialLayers {
            if (mainViewModel.shouldResetToDefault()) {
                // lấy outfit mặc định (cơ thể trống ban đầu)
                val defaultOutfit = layerSetupHelper.getOutfitJson()
                mainViewModel.setDefaultOutfit(defaultOutfit)
                layerSetupHelper.setOutfitJson(defaultOutfit)
                Log.d("CategoryFragment", "Reset outfit về mặc định")
            } else {
                val outfitJson = mainViewModel.currentOutfit.value
                if (outfitJson.isNotEmpty()) {
                    layerSetupHelper.setOutfitJson(outfitJson)
                    Log.d("CategoryFragment", "Load outfit đã chọn: $outfitJson")
                }
            }
        }

        // 2. Setup feature + animation (cần truyền layerSetupHelper vào)
        categoryLayerHelper = CategoryLayerHelper(binding, layerSetupHelper)
        categoryLayerHelper.setupFeatureAdapter()
    }

    private fun setupRecyclerViews() {
        binding.rcvCategoriesTab.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryTabAdapter
        }
    }

    private fun setupHomeButton() {
        binding.btnHome.setOnClickListener {
            saveOutfitThenBack()
        }
    }

    private fun setupResetButton() {
        binding.btnReset.setOnClickListener { layerSetupHelper.resetToInitialState() }
    }

    private fun setupSwitchCharacterButton() {
        binding.btnChangeMale.setOnClickListener { categoryLayerHelper.switchCharacter() }
    }

    private fun setupCameraButton() {
        binding.btnCamera.setOnClickListener {
            saveOutfitThenNext()
        }
    }

    private fun onCategorySelected(category: CategoryItem) {
        scrollArtsToStart()
        loadArtsByCategory()
        categoryLayerHelper.showCategoryFeatures(category.type)
    }

    private fun scrollArtsToStart() {
        binding.rcvArts.scrollToPosition(0)
    }
    private fun saveOutfitThenBack() {
        val outfitJson = layerSetupHelper.getOutfitJson()
        if (outfitJson.isNotEmpty()) {
            mainViewModel.updateCurrentOutfit(outfitJson)
            mainViewModel.commitCurrentOutfit()
        }
        mainViewModel.markResetOnNextEnter() // lần sau vào lại -> mặc định
        popBackStack()
    }
    private fun saveOutfitThenNext() {
        val outfitJson = layerSetupHelper.getOutfitJson()
        mainViewModel.updateCurrentOutfit(outfitJson)
        mainViewModel.commitCurrentOutfit()
        navigate(R.id.photographFragment)
    }
    private fun loadArtsByCategory() {
        viewModel.getArtsByCategory(categoryTabAdapter.getSelectedPosition())
    }

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

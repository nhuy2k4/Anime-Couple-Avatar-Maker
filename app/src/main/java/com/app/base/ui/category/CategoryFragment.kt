package com.app.base.ui.category
import com.app.base.R
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
import com.brally.mobile.base.activity.navigate

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
        setupHomeButton()
        setupResetButton()
        setupSwitchCharacterButton()
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    private fun initLayerHelper() {
        layerHelper = CategoryLayerHelper(binding)
        layerHelper.setupFeatureAdapter()
        layerHelper.setupInitialLayers()
    }

    private fun setupRecyclerViews() {
        binding.rcvCategoriesTab.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryTabAdapter
        }
    }


    private fun setupHomeButton() {
        binding.btnHome.setOnClickListener {
            handleBack()
        }
    }
    private fun setupResetButton() = binding.btnReset.setOnClickListener { layerHelper.resetToInitialState() }
    private fun setupSwitchCharacterButton() = binding.btnChangeMale.setOnClickListener { layerHelper.switchCharacter() }

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

    private fun handleNext() {
        val outfit = layerHelper.getAllOutfits()
        mainViewModel.saveOutfit(outfit)
        //navigate(R.id.photographFragment)
    }
    private fun handleBack() {
        val outfit = layerHelper.getAllOutfits()
        mainViewModel.saveOutfit(outfit)
        popBackStack()
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

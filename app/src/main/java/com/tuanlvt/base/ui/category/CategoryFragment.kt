package com.tuanlvt.base.ui.category

import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.utils.collectLatestFlow
import com.brally.mobile.utils.singleClick
import com.tuanlvt.base.databinding.FragmentCategoryBinding
import com.tuanlvt.base.session.isFirstScene
import com.tuanlvt.base.session.setFirstScene
import com.tuanlvt.base.ui.main.MainViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val artAdapter by lazy { ArtAdapter() }
    private val categoryTabAdapter by lazy {
        CategoryTabAdapter(onItemTapped = {
            binding.rcvArts.scrollToPosition(0)
            viewModel.getArtsByCategory(viewModel.getCategoryPosition(it))
        })
    }

    override fun initView() {
        if (isFirstScene(clazz = this)) {
            setFirstScene(clazz = this, isFirst = false)
        }
        adjustInsetsForBottomNavigation(binding.top)
        updateUI()

        // Setup category tabs
        binding.rcvCategoriesTab.adapter = categoryTabAdapter
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
        popBackStack()
    }

    override fun initData() {
        collectLatestFlow(viewModel.listArt) { data ->
            artAdapter.addData(data)
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
}

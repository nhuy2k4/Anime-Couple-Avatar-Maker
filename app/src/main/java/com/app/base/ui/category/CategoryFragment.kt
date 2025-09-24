package com.app.base.ui.category

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.core.layer.CategoryLayerHelper
import com.app.base.core.layer.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.database.OutfitEntity
import com.app.base.databinding.FragmentCategoryBinding
import com.app.base.ui.dialog.DialogSaveFragment
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.utils.collectLatestFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {
    private val args: CategoryFragmentArgs by navArgs()
    private val categoryTabAdapter by lazy { CategoryTabAdapter(onItemTapped = ::onCategorySelected) }

    private lateinit var layerSetupHelper: LayerSetupHelper
    private lateinit var categoryLayerHelper: CategoryLayerHelper

    private var outfitId: Long = -1L

    override fun initView() {
        setupRecyclerViews()
    }

    override fun initListener() {
        binding.btnHome.setOnClickListener { showSaveDialog { navigate(R.id.homeFragment) } }
        binding.btnReset.setOnClickListener { layerSetupHelper.resetToInitialState() }
        binding.btnChangeMale.setOnClickListener { categoryLayerHelper.switchCharacter() }
        binding.btnCamera.setOnClickListener { saveCurrentOutfitAndNext() }
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isNewOutfit = args.outfitId == -1L

        // 1️⃣ Init helpers trên Main thread
        layerSetupHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())
        categoryLayerHelper = CategoryLayerHelper(binding, layerSetupHelper) { _, _ ->
            updateOutfitInDb()
        }
        categoryLayerHelper.setupFeatureAdapter()
        categoryLayerHelper.setupDefaultTab()

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()
            if (isNewOutfit) {
                val entity = OutfitEntity(outfitJson = "{}", backgroundUri = null)
                outfitId = dao.insert(entity)
                withContext(Dispatchers.Main) {
                    layerSetupHelper.setupInitialLayers {
                        layerSetupHelper.setOutfitJsonWithBackground("{}")
                    }
                }
            } else {
                outfitId = args.outfitId
                val outfit = dao.getOutfitById(outfitId)
                withContext(Dispatchers.Main) {
                    outfit?.let {
                        val uri = it.backgroundUri?.let(Uri::parse)
                        layerSetupHelper.setupInitialLayers(onReady = {
                            layerSetupHelper.setOutfitJsonWithBackground(it.outfitJson, uri)
                        })
                    }
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.rcvCategoriesTab.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryTabAdapter
        }
    }

    private fun onCategorySelected(category: CategoryItem) {
        binding.rcvArts.scrollToPosition(0)
        categoryLayerHelper.showCategoryFeatures(category.type)
    }

    private fun showSaveDialog(onComplete: () -> Unit) {
        DialogSaveFragment().show(parentFragmentManager, "DialogSave")
        parentFragmentManager.setFragmentResultListener("dialog_save_request", viewLifecycleOwner) { _, bundle ->
            val result = bundle.getString("result")
            if (result == "YES") {
                updateOutfitInDb { onComplete() }
            }
        }
    }

    private fun saveCurrentOutfitAndNext() {
        updateOutfitInDb {
            val action = CategoryFragmentDirections.actionCategoryFragmentToPhotographFragment(outfitId)
            Log.d("CategoryFragment", "Navigate to Photograph with outfitId=$outfitId")
            findNavController().navigate(action)
        }
    }

    private fun updateOutfitInDb(onComplete: (() -> Unit)? = null) {
        val outfitJson = layerSetupHelper.getFullOutfitJson()
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()
            val outfit = dao.getOutfitById(outfitId)
            outfit?.let {
                dao.update(it.copy(outfitJson = outfitJson))
                Log.d("CategoryFragment", "Auto updated outfit $outfitId")
            }
            withContext(Dispatchers.Main) { onComplete?.invoke() }
        }
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

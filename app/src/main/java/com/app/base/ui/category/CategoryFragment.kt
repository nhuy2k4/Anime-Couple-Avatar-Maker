package com.app.base.ui.category

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.core.layer.CategoryLayerHelper
import com.app.base.core.layer.LayerSetupHelper
import com.app.base.core.utils.PhotoCommitHelper
import com.app.base.databinding.FragmentCategoryBinding
import com.app.base.ui.dialog.DialogSaveFragment
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.utils.collectLatestFlow
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.app.base.ui.main.MainViewModel

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args: CategoryFragmentArgs by navArgs()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val categoryTabAdapter by lazy { CategoryTabAdapter(onItemTapped = ::onCategorySelected) }

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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mainViewModel.currentOutfit.value == null) {
            // Khởi tạo outfit mới ngay khi vào Category
            val newOutfitJson = "{}" // hoặc outfit json mặc định
            mainViewModel.createNewOutfit(newOutfitJson)
        }
    }

    private fun initHelpers() {
        // Layer setup
        layerSetupHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())
        layerSetupHelper.setupInitialLayers {
            // Nếu lần vào này cần reset (từ Home), set mặc định
            if (mainViewModel.shouldResetToDefault()) {
                layerSetupHelper.setOutfitJsonWithBackground("{}")
            } else {
                // Load outfit hiện tại
                mainViewModel.currentOutfit.value?.let { outfit ->
                    if (outfit.json.isNotEmpty()) layerSetupHelper.setOutfitJson(outfit.json)
                } ?: layerSetupHelper.setOutfitJsonWithBackground("{}")
            }
        }

        // Category features setup
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
        binding.btnHome.setOnClickListener { saveOutfitThenBack() }
    }

    private fun setupResetButton() {
        binding.btnReset.setOnClickListener { layerSetupHelper.resetToInitialState() }
    }

    private fun setupSwitchCharacterButton() {
        binding.btnChangeMale.setOnClickListener { categoryLayerHelper.switchCharacter() }
    }

    private fun setupCameraButton() {
        binding.btnCamera.setOnClickListener { saveOutfitThenNext() }
    }

    private fun onCategorySelected(category: CategoryItem) {
        binding.rcvArts.scrollToPosition(0)
        categoryLayerHelper.showCategoryFeatures(category.type)
        // Chỉ preview, không commit tạm vào ViewModel
    }

    private fun saveOutfitThenBack() {
        DialogSaveFragment().show(parentFragmentManager, "DialogSave")
        parentFragmentManager.setFragmentResultListener("dialog_save_request", viewLifecycleOwner) { _, bundle ->
            val result = bundle.getString("result")
            if (result == "YES") {
                val outfitJson = layerSetupHelper.getOutfitJsonWithBackground(R.drawable.bg_gradient)
                PhotoCommitHelper.commitFromContainer(binding.photoContainer, outfitJson, mainViewModel) {
                    // Clear tạm nếu có
                    PhotoCommitHelper.clearTemporaryOutfits(mainViewModel)
                    navigate(R.id.homeFragment)
                }
            }
        }
    }

    private fun saveOutfitThenNext() {
        val outfitJsonWithBg = layerSetupHelper.getOutfitJsonWithBackground(R.drawable.bg_gradient)
        if (outfitJsonWithBg.isNotEmpty()) {
            val currentUri = mainViewModel.currentOutfit.value?.uri
            // Chỉ cập nhật currentOutfit trong ViewModel, không lưu bitmap
            mainViewModel.commitOutfitWithBitmap(outfitJsonWithBg, null, currentUri)
            navigate(R.id.photographFragment)
        } else {
            navigate(R.id.photographFragment)
        }
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

    override fun onResume() {
        super.onResume()
        // Khi quay lại fragment này, reset mặc định nếu cần
        if (mainViewModel.shouldResetToDefault()) {
            layerSetupHelper.setOutfitJsonWithBackground("{}")
        } else {
            mainViewModel.currentOutfit.value?.let {
                layerSetupHelper.setOutfitJson(it.json)
            }
        }
    }

    private fun logCurrentOutfits() {
        val outfits = mainViewModel.outfitListData.value
        Log.d("OutfitListDebug", "=== Current outfits ===")
        outfits.forEachIndexed { index, outfit ->
            Log.d("OutfitListDebug", "$index: json=${outfit.json}, uri=${outfit.uri}")
        }
        Log.d("OutfitListDebug", "=== End of list ===")
    }
}

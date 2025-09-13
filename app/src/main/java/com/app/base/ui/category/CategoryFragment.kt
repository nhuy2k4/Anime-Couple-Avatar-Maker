package com.app.base.ui.category

import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.databinding.FragmentCategoryBinding
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.onBackPressed
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.ads.AdManager.showFull
import com.brally.mobile.utils.collectLatestFlow
import ja.burhanrashid52.photoeditor.PhotoEditor
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args by navArgs<CategoryFragmentArgs>()
    private val mainViewModel by activityViewModel<MainViewModel>()

    private val categoryTabAdapter by lazy { CategoryTabAdapter(onItemTapped = ::onCategorySelected) }

    private val categoryId by lazy { args.categoryId }
    private val dummyData = mapOf(
        "hair" to listOf(R.drawable.hair1, R.drawable.hair2),
        "eye"  to listOf(R.drawable.eye1, R.drawable.eye2),
    )
    private lateinit var photoEditor: PhotoEditor
    private lateinit var layerManager: LayerManager

    // Feature chung
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter

    override fun initView() {
        setupFeatureAdapter()
        setupRecyclerViews()
        // Khởi tạo PhotoEditor + LayerManager
        photoEditor = PhotoEditor.Builder(requireContext(), binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()
        layerManager = LayerManager(binding.photoContainer, requireContext())

        // Load base nam + nữ sau khi container measured để tránh size 0
        binding.photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                layerManager.setLayer("base_male", R.drawable.body_base_male)
                layerManager.setLayer("base_female", R.drawable.body_base_female)
            }
        })
    }

    private fun setupFeatureAdapter() {
        featureAdapter = LayerItemAdapter(mutableListOf()) { resId ->
            currentFeatureType?.let { type ->
                when (type) {
                    "hair" -> layerManager.setLayer(
                        type = type,
                        resId = resId,
                        widthRatio = 1f,      // Kích thước chung cho hair
                        heightRatio = 0.5f,
                        topMarginRatio = 0.15f
                    )
                    "eye" -> layerManager.setLayer(
                        type = type,
                        resId = resId,
                        widthRatio = 0.33f,      // Kích thước chung cho eye
                        heightRatio = 0.15f,
                        topMarginRatio = 0.4f
                    )
                    else -> layerManager.setLayer(
                        type = type,
                        resId = resId,
                        widthRatio = 0.5f,      // Kích thước mặc định
                        heightRatio = 0.5f,
                        topMarginRatio = 0.0f
                    )
                }
            }
        }


        binding.rcvArts.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = featureAdapter
            visibility = View.GONE
        }
    }

    private fun showFeatureItems(type: String, items: List<Int>) {
        currentFeatureType = type
        featureAdapter.updateData(items)
        binding.rcvArts.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerViews() {
        // Categories tab
        binding.rcvCategoriesTab.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryTabAdapter
        }
    }

    override fun initListener() {
        setupBackPressListener()
        setupHomeButtonListener()

        binding.btnReset.setOnClickListener {
            layerManager.clearLayers(keepBase = true)
        }
    }

    override fun initData() {
        viewModel.loadInitialData()  // Load từ ViewModel
        observeViewModel()
    }

    private fun setupBackPressListener() {
        onBackPressed { handleBackPress() }
    }

    private fun setupHomeButtonListener() {
        binding.btnHome.setOnClickListener { handleBackPress() }
    }

    private fun onCategorySelected(category: CategoryItem) {
        binding.rcvArts.scrollToPosition(0)
        viewModel.getArtsByCategory(categoryTabAdapter.getSelectedPosition())
        dummyData[category.type]?.let { items ->
            showFeatureItems(category.type, items)
        } ?: run {
            binding.rcvArts.visibility = View.GONE
        }
    }

    private fun onArtItemClicked(artItem: ArtItem) {
        // TODO: Navigate or handle art click
    }

    private fun handleBackPress() {
        showFull(AdManager.FULL_DRAW) { popBackStack() }
    }

    private fun observeViewModel() {
        collectLatestFlow(viewModel.categories) { categories ->
            if (categories.isNotEmpty()) {
                categoryTabAdapter.setCategories(categories)
            } else {
                categoryTabAdapter.clearCategories()
            }
        }

        collectLatestFlow(viewModel.categorySelected) { pos ->
            categoryTabAdapter.selectCategory(pos)
        }

        collectLatestFlow(viewModel.listArt) { arts ->
            // Nếu dùng ArtAdapter, setup ở đây; hiện tại không dùng nên bỏ
        }
    }
}
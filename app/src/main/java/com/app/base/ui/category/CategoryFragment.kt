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

    // Tạo data theo nhân vật
    private val dummyData = mapOf(
        "male" to mapOf(
            "hair" to listOf(R.drawable.hair3, R.drawable.hair4),
            "eye"  to listOf(R.drawable.eye3, R.drawable.eye4)
        ),
        "female" to mapOf(
            "hair" to listOf(R.drawable.hair1, R.drawable.hair2),
            "eye"  to listOf(R.drawable.eye1, R.drawable.eye2)
        )
    )

    private var currentCharacter: String = "female"
    private lateinit var photoEditor: PhotoEditor
    private lateinit var layerManager: LayerManager

    // Feature chung
    private var currentFeatureType: String? = null
    private lateinit var featureAdapter: LayerItemAdapter

    // Lưu trạng thái các feature của mỗi nhân vật
    private val selectedFeatures = mutableMapOf(
        "male" to mutableMapOf<String, Int>(),
        "female" to mutableMapOf<String, Int>()
    )

    override fun initView() {
        setupFeatureAdapter()
        setupRecyclerViews()

        photoEditor = PhotoEditor.Builder(requireContext(), binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()
        layerManager = LayerManager(binding.photoContainer, requireContext())

        binding.photoContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.photoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val shiftLeft = 0.15f
                val leftMarginFemaleRatio = 0.3f

                // ----- Body -----
                layerManager.setLayer(
                    type = "male-body",
                    resId = R.drawable.body_base_male,
                    widthRatio = 1f,
                    heightRatio = 1f,
                    leftMarginRatio = 0f - shiftLeft
                )
                layerManager.setLayer(
                    type = "female-body",
                    resId = R.drawable.body_base_female,
                    widthRatio = 1f,
                    heightRatio = 1f,
                    leftMarginRatio = leftMarginFemaleRatio - shiftLeft
                )

                // ----- Hair & Eye mặc định -----
                listOf("male", "female").forEach { character ->
                    val leftMargin = if (character == "male") 0f - shiftLeft else leftMarginFemaleRatio - shiftLeft
                    // Hair mặc định
                    dummyData[character]?.get("hair")?.firstOrNull()?.let { resId ->
                        layerManager.setLayer(
                            type = "$character-hair",
                            resId = resId,
                            widthRatio = 1f,
                            heightRatio = 1f,
                            leftMarginRatio = leftMargin
                        )
                        selectedFeatures[character]?.put("hair", resId)
                    }
                    // Eye mặc định
                    dummyData[character]?.get("eye")?.firstOrNull()?.let { resId ->
                        layerManager.setLayer(
                            type = "$character-eye",
                            resId = resId,
                            widthRatio = 1f,
                            heightRatio = 1f,
                            leftMarginRatio = leftMargin
                        )
                        selectedFeatures[character]?.put("eye", resId)
                    }
                }

                // ----- Popup animation cho toàn bộ -----
                val allLayers = layerManager.getLayersForCharacter("male") + layerManager.getLayersForCharacter("female")
                allLayers.forEach { view ->
                    view.scaleX = 0.9f
                    view.scaleY = 0.9f
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
            }
        })
    }


    private fun setupFeatureAdapter() {
        val shiftLeft = 0.15f
        val leftMarginFemaleRatio = 0.15f

        featureAdapter = LayerItemAdapter(mutableListOf()) { resId ->
            currentFeatureType?.let { type ->
                val leftMargin = if (currentCharacter == "male") 0f - shiftLeft else leftMarginFemaleRatio
                val layerType = "$currentCharacter-$type"

                // Đặt layer feature cho nhân vật hiện tại
                layerManager.setLayer(
                    type = layerType,
                    resId = resId,
                    widthRatio = 1f,
                    heightRatio = 1f,
                    leftMarginRatio = leftMargin
                )

                // Lưu trạng thái cho nhân vật
                selectedFeatures[currentCharacter]?.set(type, resId)
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

        // Tự động hiển thị feature đã chọn của nhân vật
        selectedFeatures[currentCharacter]?.get(type)?.let { resId ->
            val layerType = "$currentCharacter-$type"
            val leftMargin = if (currentCharacter == "male") 0f - 0.15f else 0.15f
            layerManager.setLayer(
                type = layerType,
                resId = resId,
                widthRatio = 1f,
                heightRatio = 1f,
                leftMarginRatio = leftMargin
            )
        }

        binding.rcvArts.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerViews() {
        binding.rcvCategoriesTab.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryTabAdapter
        }
    }



    override fun initListener() {
        setupBackPressListener()
        setupHomeButtonListener()

        binding.btnReset.setOnClickListener {
            // Chỉ xóa feature, giữ body
            layerManager.clearLayers(keepBase = true)
            selectedFeatures["male"]?.clear()
            selectedFeatures["female"]?.clear()
        }

        binding.btnChangeMale.setOnClickListener {
            // Đổi nhân vật
            currentCharacter = if (currentCharacter == "male") "female" else "male"

            // Load lại features cho nhân vật mới
            currentFeatureType?.let { type ->
                dummyData[currentCharacter]?.get(type)?.let { items ->
                    showFeatureItems(type, items)
                }
            }

            // Lấy tất cả layer của nhân vật mới
            val characterLayers = layerManager.getLayersForCharacter(currentCharacter)

            // Animate tất cả layer cùng lúc
            characterLayers.forEach { view ->
                view.scaleX = 1f
                view.scaleY = 1f
                view.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
            }
        }


    }

    override fun initData() {
        viewModel.loadInitialData()
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

        val type = category.type
        currentFeatureType = type

        dummyData[currentCharacter]?.get(type)?.let { items ->
            showFeatureItems(type, items)
        } ?: run {
            binding.rcvArts.visibility = View.GONE
        }
    }

    private fun onArtItemClicked(artItem: ArtItem) {
        // TODO
    }

    private fun handleBackPress() {
        showFull(AdManager.FULL_DRAW) { popBackStack() }
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

package com.app.base.ui.category

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.core.helper.CategoryLayerHelper
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.database.FeatureRepository
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
import org.json.JSONObject

class CategoryFragment : BaseFragment<FragmentCategoryBinding, CategoryViewModel>() {

    private val args: CategoryFragmentArgs by navArgs()
    private val categoryTabAdapter by lazy { CategoryTabAdapter(onItemTapped = ::onCategorySelected) }

    private lateinit var layerSetupHelper: LayerSetupHelper
    private lateinit var categoryLayerHelper: CategoryLayerHelper
    private lateinit var featureRepo: FeatureRepository
    private lateinit var allButtons: List<View>
    private var outfitId: Long = -1L
    private var isFromGallery: Boolean = false

    private var cachedCategories: List<CategoryItem> = emptyList()

    override fun initView() {
        allButtons = listOf(
            binding.btnCamera,
            binding.btnGoPK,
            binding.btnWand,
            binding.btnReset,
            binding.btnChangeSkin,
            binding.btnChangeMale,
            binding.similar,
            binding.previewCos
        )

        setupRecyclerViews()
        when (args.mode) {
            "B" -> setupModeB()
            "C" -> setupModeC()
            else -> setupModeA()
        }
    }

    private fun setButtonVisibility(visibleButtons: List<View>) {
        allButtons.forEach { it.visibility = if (it in visibleButtons) View.VISIBLE else View.GONE }
    }

    private fun setupModeA() {
        setButtonVisibility(listOf(binding.btnCamera, binding.btnReset, binding.btnChangeSkin, binding.btnChangeMale))
    }

    private fun setupModeC() {
        setButtonVisibility(listOf(binding.similar, binding.btnWand, binding.previewCos))
    }

    private fun setupModeB() {
        setButtonVisibility(listOf(binding.btnGoPK, binding.btnChangeMale, binding.btnReset, binding.btnChangeSkin))
    }

    override fun initListener() {
        binding.btnHome.setOnClickListener { showSaveDialog { navigate(R.id.homeFragment) } }
        binding.btnReset.setOnClickListener { layerSetupHelper.resetToInitialState() }
        binding.btnChangeMale.setOnClickListener { categoryLayerHelper.switchCharacter() }

        when (args.mode) {
            "B" -> binding.btnGoPK.setOnClickListener { prepareBattle() }
            "C" -> binding.btnWand.setOnClickListener {
                navigate(R.id.checkPointFragment, Bundle().apply { putString("sourceMode", "C") })
            }
            else -> binding.btnCamera.setOnClickListener { saveCurrentOutfitAndNext() }
        }
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        outfitId = args.outfitId
        isFromGallery = args.isFromGallery

        layerSetupHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())
        featureRepo = FeatureRepository(requireContext())
        categoryLayerHelper = CategoryLayerHelper(
            binding,
            layerSetupHelper,
            onFeatureUpdated = { _, _ -> updateOutfitInDb() },
            onCharacterChange = {
                val filtered = filterCategories(cachedCategories)
                if (filtered.isNotEmpty()) categoryTabAdapter.setCategories(filtered)
                else categoryTabAdapter.clearCategories()
            }
        )

        categoryLayerHelper.setupFeatureAdapter()
        categoryLayerHelper.setupDefaultTab()

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()

            if (outfitId != -1L) {
                // outfit có sẵn (từ gallery)
                dao.getOutfitById(outfitId)?.let { outfit ->
                    withContext(Dispatchers.Main) {
                        setupOutfitFromEntity(outfit)
                    }
                }
            } else {
                // outfit mới – lấy từ photo nếu có, fallback mặc định
                val jsonFromPhoto = args.outfitJson
                withContext(Dispatchers.Main) {
                    if (!jsonFromPhoto.isNullOrEmpty()) {
                        // setup container thôi, offsets = 0
                        layerSetupHelper.setupInitialLayers(
                            listOf("male", "female"),
                            offsetsX = mapOf("male" to 150f, "female" to -200f)
                        ) {
                            // JSON từ Photo sẽ restore vị trí đúng
                            val bgId = extractBackgroundId(jsonFromPhoto)
                            layerSetupHelper.setupInitialLayers(
                                listOf("male", "female"),
                                offsetsX = mapOf("male" to 150f, "female" to -200f)
                            ) {
                                layerSetupHelper.setOutfitJsonWithBackground(jsonFromPhoto) // JSON có features + background
                                if (bgId != null) {
                                    //layerSetupHelper.setBackground(bgId) // nếu bạn có method setBackground riêng
                                }
                            }

                        }
                    } else {
                        // fallback – tạo layout mặc định
                        val initialBackgroundId = if (args.mode == "B") args.backgroundId else null
                        setupInitialLayers(initialBackgroundId)
                    }
                }

            }
        }
    }


    private fun setupOutfitFromEntity(outfit: OutfitEntity) {
        val isSingleCharacterMode = args.mode == "C"
        val characterList = if (isSingleCharacterMode) listOf(args.gender) else listOf("male", "female")
        val offsets = if (isSingleCharacterMode) {
            mapOf(args.gender to 0f)
        } else {
            mapOf("male" to 150f, "female" to -200f)
        }

        layerSetupHelper.setupInitialLayers(characterList, offsetsX = offsets) {
            val nested = mutableMapOf<String, MutableMap<String, String>>()
            outfit.features.forEach { (k, v) ->
                val parts = k.split('.')
                if (parts.size == 2) {
                    val char = parts[0]
                    val layer = parts[1]
                    val mapForChar = nested.getOrPut(char) { mutableMapOf() }
                    mapForChar[layer] = v
                }
            }
            layerSetupHelper.setFeaturesMap(nested)

            outfit.backgroundId?.let { bg ->
                layerSetupHelper.setOutfitJsonWithBackground(
                    layerSetupHelper.getFullOutfitJson(backgroundId = bg)
                )
            }
        }
    }
    private fun extractBackgroundId(json: String?): String? {
        if (json.isNullOrEmpty()) return null
        return try {
            val obj = JSONObject(json)
            obj.optString("backgroundId", null)
        } catch (e: Exception) { null }
    }

    private fun setupInitialLayers(backgroundId: String?) {
        val isSingleCharacterMode = args.mode == "C"
        val characterList = if (isSingleCharacterMode) listOf(args.gender) else listOf("male", "female")
        val offsets = if (isSingleCharacterMode) mapOf(args.gender to 0f) else mapOf("male" to 150f, "female" to -200f)

        layerSetupHelper.setupInitialLayers(characterList, offsetsX = offsets) {
            val json = layerSetupHelper.getFullOutfitJson(backgroundId = backgroundId)
            if (backgroundId != null) {
                layerSetupHelper.setOutfitJsonWithBackground(json)
            }
        }
    }

    private fun prepareBattle() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()
            dao.getOutfitById(outfitId)?.let { dao.update(it.copy(backgroundId = null)) }
            withContext(Dispatchers.Main) { navigate(R.id.waitingFragment) }
        }
    }

    private fun showSaveDialog(onComplete: () -> Unit) {
        DialogSaveFragment().show(parentFragmentManager, "DialogSave")
        parentFragmentManager.setFragmentResultListener("dialog_save_request", viewLifecycleOwner) { _, bundle ->
            if (bundle.getString("result") == "YES") {
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                    val outfit = dao.getOutfitById(outfitId)
                    outfit?.let {
                        val featuresFlat = layerSetupHelper.getFeaturesAsStringMap()
//                        val bgId = extractBackgroundId()
//                        if (bgId == null && it.thumbnailPath == null) {
//                            dao.deleteOutfit(it.id)
//                            Log.d("CategoryFragment", "Deleted empty outfit $outfitId")
//                        } else {
//                            dao.update(it.copy(features = featuresFlat, backgroundId = bgId))
//                            Log.d("CategoryFragment", "Updated outfit $outfitId before exit")
//                        }
                    }
                    withContext(Dispatchers.Main) { onComplete() }
                }
            }
        }
    }

//    private fun extractBackgroundId(): String? {
//        return try {
//            val bgId = JSONObject(layerSetupHelper.getOutfitJson()).optInt("backgroundId", 0)
//            if (bgId != 0) bgId.toString() else null
//        } catch (e: Exception) { null }
//    }

    private fun saveCurrentOutfitAndNext() {
        val json = layerSetupHelper.getFullOutfitJson()
        val action = CategoryFragmentDirections.actionCategoryFragmentToPhotographFragment(
            outfitId = outfitId,
            isFromGallery = isFromGallery,
            outfitJson = json
        )
        findNavController().navigate(action)
    }

    private fun updateOutfitInDb(onComplete: (() -> Unit)? = null) {
        val featuresFlat = layerSetupHelper.getFeaturesAsStringMap()
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()
            dao.getOutfitById(outfitId)?.let {
                val bgStr = it.backgroundId
                dao.update(it.copy(features = featuresFlat, backgroundId = bgStr))
                Log.d("CategoryFragment", "Auto updated outfit $outfitId")
            }
            withContext(Dispatchers.Main) { onComplete?.invoke() }
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

    private fun loadViewModelData() = viewModel.loadInitialData()

    private fun observeViewModel() {
        collectLatestFlow(viewModel.categories) { categories ->
            cachedCategories = categories
            val filtered = filterCategories(categories)
            if (filtered.isNotEmpty()) categoryTabAdapter.setCategories(filtered)
            else categoryTabAdapter.clearCategories()
        }
        collectLatestFlow(viewModel.categorySelected) { pos ->
            categoryTabAdapter.selectCategory(pos)
        }
    }

    private fun filterCategories(categories: List<CategoryItem>): List<CategoryItem> {
        val gender = if (::categoryLayerHelper.isInitialized) categoryLayerHelper.getCurrentCharacter() else "female"
        return categories.filter { item ->
            try {
                featureRepo.hasFeaturesFor(item.type, gender)
            } catch (e: Exception) {
                true
            }
        }
    }
}

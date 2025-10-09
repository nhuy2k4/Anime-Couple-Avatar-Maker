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
    private lateinit var allButtons: List<View>
    private var outfitId: Long = -1L

    override fun initView() {
        allButtons = listOf(
            binding.btnCamera,
            binding.btnGoPK,
            binding.btnWand,
            binding.btnReset,
            binding.btnChangeSkin,
            binding.btnChangeMale,
            binding.similar,
            binding.btnWand,
            binding.previewCos
        )
        setupRecyclerViews()
        when (args.mode) {
            "B" -> setupModeB() // Thiết lập cho chế độ Battle/PK
            "C" -> setupModeC() // Thiết lập cho chế độ Cosplay
            else -> setupModeA() // Mặc định là chế độ A
        }
    }
    private fun setButtonVisibility(visibleButtons: List<View>) {
        allButtons.forEach { button ->
            button.visibility = if (button in visibleButtons) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
    private fun setupModeA() {
        setButtonVisibility(listOf(
            binding.btnCamera,
            binding.btnReset,
            binding.btnChangeSkin,
            binding.btnChangeMale
        ))
    }
    private fun setupModeC() {
        setButtonVisibility(listOf(
            binding.similar,
            binding.btnWand,
            binding.previewCos
        ))
    }
    private fun setupModeB() {
        setButtonVisibility(listOf(
            binding.btnGoPK,
            binding.btnChangeMale,
            binding.btnReset,
            binding.btnChangeSkin

        ))
    }
    override fun initListener() {
        binding.btnHome.setOnClickListener { showSaveDialog { navigate(R.id.homeFragment) } }
        binding.btnReset.setOnClickListener { layerSetupHelper.resetToInitialState() }
        // ✅ Chỉ toggle khi nhấn nút
        binding.btnChangeMale.setOnClickListener { categoryLayerHelper.switchCharacter() }

        // Nếu bạn muốn chọn nhân vật cố định từ UI, ví dụ avatar trong RecyclerView
        // someMaleAvatar.setOnClickListener { categoryLayerHelper.setCharacter("male") }
        // someFemaleAvatar.setOnClickListener { categoryLayerHelper.setCharacter("female") }
        when (args.mode) {
            "B" -> {
                binding.btnGoPK.setOnClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                        val outfit = dao.getOutfitById(outfitId)

                        if (outfit != null) {
                            // Remove backgroundId (now a separate column)
                            dao.update(outfit.copy(backgroundId = null))
                            Log.d("CategoryFragment", "BackgroundId removed for outfit $outfitId")
                        }

                        withContext(Dispatchers.Main) {
                            navigate(R.id.waitingFragment)
                        }
                    }
                }
            }
            "C" -> binding.btnWand.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("sourceMode", "C")
                }
                navigate(R.id.checkPointFragment, bundle)

            }
            else -> binding.btnCamera.setOnClickListener { saveCurrentOutfitAndNext() }
        }
    }
    private fun removeBackgroundId(json: String): String {
        val jsonObject = JSONObject(json)
        jsonObject.remove("backgroundId")
        return jsonObject.toString()
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isNewOutfit = args.outfitId == -1L

        // 1️⃣ Init helpers trên Main thread, thêm callback onCharacterChange
        layerSetupHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())
        categoryLayerHelper = CategoryLayerHelper(
            binding,
            layerSetupHelper,
            onFeatureUpdated = { _, _ -> updateOutfitInDb() },
            onCharacterChange = {
                // Có thể log, play animation hoặc auto update DB nếu cần
                //Log.d("CategoryFragment", "Character changed to ${it}")
            }
        )
        categoryLayerHelper.setupFeatureAdapter()
        categoryLayerHelper.setupDefaultTab()

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()
            if (isNewOutfit) {
                // outfitJson mặc định
                // Prepare initial features/background when creating new outfit
                val initialBackgroundId = if (args.mode == "B") args.backgroundId else null
                val featuresMap = emptyMap<String, String>()
                val entity = OutfitEntity(features = featuresMap, backgroundId = initialBackgroundId?.toString(), thumbnailPath = null)
                val rowId = dao.insert(entity)
                outfitId = rowId

                withContext(Dispatchers.Main) {
                    if (args.mode == "C") {
                        layerSetupHelper.setupInitialLayers(listOf(args.gender),
                                offsetsX = mapOf(
                                "male" to 30f,     // căn trái
                            "female" to -10f)  // căn phải
                        )
                    } else
                    layerSetupHelper.setupInitialLayers(listOf("male", "female"),
                        offsetsX = mapOf(
                            "male" to 150f,     // căn trái
                            "female" to -200f) ) {
                        // If background provided, set it via JSON-compatible API
                        initialBackgroundId?.let {
                            val json = layerSetupHelper.getFullOutfitJson(backgroundId = it)
                            layerSetupHelper.setOutfitJsonWithBackground(json)
                        }
                    }
                }
            } else {
                // outfit cũ
                outfitId = args.outfitId
                val outfit = dao.getOutfitById(outfitId)
                withContext(Dispatchers.Main) {
                    outfit?.let {
                        layerSetupHelper.setupInitialLayers(listOf("male", "female"),
                            offsetsX = mapOf(
                                "male" to 30f,     // căn trái
                                "female" to -10f)
                            , onReady = {
                            // convert flat features map to nested Map<String, Map<String, Int>>
                            val nested = mutableMapOf<String, MutableMap<String, Int>>()
                            it.features.forEach { (k, v) ->
                                val parts = k.split('.')
                                if (parts.size == 2) {
                                    val char = parts[0]
                                    val layer = parts[1]
                                    val mapForChar = nested.getOrPut(char) { mutableMapOf() }
                                    mapForChar[layer] = v.toIntOrNull() ?: 0
                                }
                            }
                            layerSetupHelper.setFeaturesMap(nested)
                            // set background if exists
                            it.backgroundId?.toIntOrNull()?.let { bg ->
                                // ensure backgroundId is included in JSON passed to helper
                                layerSetupHelper.setOutfitJsonWithBackground(layerSetupHelper.getFullOutfitJson(backgroundId = bg))
                            }
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
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                    val outfit = dao.getOutfitById(outfitId)

                    if (outfit != null) {
                        if (outfit.backgroundId == null && outfit.thumbnailPath == null) {
                            // ❌ outfit chưa có background -> xóa
                            dao.deleteOutfit(outfit.id)
                            Log.d("CategoryFragment", "Deleted empty outfit $outfitId")
                        } else {
                            // ✅ update features/backgroundId before exit
                            val featuresFlat = layerSetupHelper.getFeaturesAsStringMap()
                            dao.update(outfit.copy(features = featuresFlat, backgroundId = outfit.backgroundId))
                            Log.d("CategoryFragment", "Updated outfit $outfitId before exit")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        onComplete() // Navigate về Home
                    }
                }
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
        val featuresFlat = layerSetupHelper.getFeaturesAsStringMap()
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(requireContext()).outfitDao()
            val outfit = dao.getOutfitById(outfitId)
            outfit?.let {
                // also persist current backgroundId from layerSetupHelper
                val currentJson = layerSetupHelper.getOutfitJson()
                val bgId = try {
                    org.json.JSONObject(currentJson).optInt("backgroundId", 0)
                } catch (e: Exception) { 0 }
                val bgStr: String? = if (bgId != 0) bgId.toString() else it.backgroundId
                dao.update(it.copy(features = featuresFlat, backgroundId = bgStr))
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






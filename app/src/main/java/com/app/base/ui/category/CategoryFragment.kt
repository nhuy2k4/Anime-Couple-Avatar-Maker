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

    private var outfitId: Long = -1L

    override fun initView() {
        setupRecyclerViews()
        if (args.mode == "B") {
            setupModeB()
        } else {
            setupModeA()
        }
    }
    private fun setupModeA() {
        binding.btnCamera.visibility = View.VISIBLE
        binding.btnGoPK.visibility = View.GONE
    }

    private fun setupModeB() {
        binding.btnCamera.visibility = View.GONE
        binding.btnGoPK.visibility = View.VISIBLE
    }
    override fun initListener() {
        binding.btnHome.setOnClickListener { showSaveDialog { navigate(R.id.homeFragment) } }
        binding.btnReset.setOnClickListener { layerSetupHelper.resetToInitialState() }

        // ✅ Chỉ toggle khi nhấn nút
        binding.btnChangeMale.setOnClickListener { categoryLayerHelper.switchCharacter() }

        // Nếu bạn muốn chọn nhân vật cố định từ UI, ví dụ avatar trong RecyclerView
        // someMaleAvatar.setOnClickListener { categoryLayerHelper.setCharacter("male") }
        // someFemaleAvatar.setOnClickListener { categoryLayerHelper.setCharacter("female") }
        if (args.mode == "B") {
            binding.btnGoPK.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                    val outfit = dao.getOutfitById(outfitId)

                    if (outfit != null) {
                        val cleanedJson = removeBackgroundId(outfit.outfitJson)
                        dao.update(outfit.copy(outfitJson = cleanedJson, backgroundUri = null))
                        Log.d("CategoryFragment", "BackgroundId removed for outfit $outfitId")
                    }

                    withContext(Dispatchers.Main) {
                        navigate(R.id.waitingFragment)
                    }
                }
            }
        } else
            binding.btnCamera.setOnClickListener { saveCurrentOutfitAndNext() }
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
                val initialJson = if (args.mode == "B") {
                    """{ "backgroundId": "${args.backgroundId}" }"""   // 🟢 Mode B -> set backgroundId luôn
                } else {
                    "{}"
                }

                val entity = OutfitEntity(outfitJson = initialJson, backgroundUri = null)
                outfitId = dao.insert(entity)

                withContext(Dispatchers.Main) {
                    layerSetupHelper.setupInitialLayers {
                        layerSetupHelper.setOutfitJsonWithBackground(initialJson)
                    }
                }
            } else {
                // outfit cũ
                outfitId = args.outfitId
                val outfit = dao.getOutfitById(outfitId)
                withContext(Dispatchers.Main) {
                    outfit?.let {
                        layerSetupHelper.setupInitialLayers(onReady = {
                            layerSetupHelper.setOutfitJsonWithBackground(it.outfitJson)
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
                        if (outfit.backgroundUri == null) {
                            // ❌ outfit chưa có backgroundUri -> xóa
                            dao.deleteOutfit(outfit.id)
                            Log.d("CategoryFragment", "Deleted empty outfit $outfitId")
                        } else {
                            // ✅ outfit có backgroundUri -> update outfitJson trước khi thoát
                            val outfitJson = layerSetupHelper.getFullOutfitJson()
                            dao.update(outfit.copy(outfitJson = outfitJson))
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

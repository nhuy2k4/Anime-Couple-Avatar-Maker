package com.app.base.ui.photograph

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.app.base.R
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.database.OutfitEntity
import com.app.base.databinding.FragmentPhotographBinding
import com.brally.mobile.base.activity.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class PhotographFragment : BaseFragment<FragmentPhotographBinding, PhotographViewModel>() {

    private val args: PhotographFragmentArgs by navArgs()
    private val photographViewModel by viewModel<PhotographViewModel>()
    private lateinit var layerSetupHelper: LayerSetupHelper
    private val photoAdapter by lazy { PhotographAdapter(onPhotoTapped = ::onPhotoSelected) }

    private var outfitId: Long? = null
    private var isFromGallery: Boolean = false

    override fun initView() {
        binding.rcvPhotographs.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
        }
    }

    override fun initListener() {
        // 🔙 Quay lại Category (Mode A)
        binding.btnBack.setOnClickListener {
            val action = PhotographFragmentDirections
                .actionPhotographFragmentToCategoryFragment(
                    outfitId = outfitId ?: -1L,
                    isFromGallery = isFromGallery,
                    outfitJson = layerSetupHelper.getOutfitJson(),
                    mode = "A"
                )
            findNavController().navigate(action)
        }

        // 💾 Lưu outfit
        binding.btnSave.setOnClickListener { saveCurrentOutfit() }
    }

    override fun initData() {
        photographViewModel.loadInitialData(requireContext())
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            photographViewModel.photos.collect { outfits ->
                if (outfits.isNotEmpty()) photoAdapter.setPhotos(outfits)
                else photoAdapter.clearPhotos()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layerSetupHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())

        outfitId = args.outfitId.takeIf { it != -1L }
        isFromGallery = args.isFromGallery
        Log.d("PhotographFragment", "🧩 outfitId=$outfitId, isFromGallery=$isFromGallery")
        if (isFromGallery && outfitId != null) {
            loadOutfitById(outfitId!!)
        } else {
            // 🆕 Outfit mới
            args.outfitJson?.let { json ->
                layerSetupHelper.setupInitialLayers(
                    listOf("male", "female"),
                    offsetsX = mapOf("male" to 170f, "female" to -170f)
                ) {
                    layerSetupHelper.setOutfitJsonWithBackground(json)
                }
            }
        }
    }

    private fun loadOutfitById(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val outfit = AppDatabase.getInstance(requireContext()).outfitDao().getOutfitById(id)
            withContext(Dispatchers.Main) {
                outfit?.let {
                    // Lấy danh sách character từ features, fallback male/female
                    val characters = if (it.features.isNotEmpty()) {
                        it.features.keys.map { k -> k.split('.')[0] }.distinct()
                    } else listOf("male", "female")

                    // Offset hợp lý để body hiển thị không chồng nhau
                    val offsets = if (characters.size == 1) mapOf(characters[0] to 0f)
                    else mapOf("male" to 140f, "female" to -140f)

                    // Setup initial layers
                    layerSetupHelper.setupInitialLayers(characters, offsetsX = offsets) {
                        // Build nested features map
                        val nested = mutableMapOf<String, MutableMap<String, String>>()
                        it.features.forEach { (k, v) ->
                            val parts = k.split('.')
                            if (parts.size == 2) {
                                val char = parts[0]
                                val layer = parts[1]
                                nested.getOrPut(char) { mutableMapOf() }[layer] = v // giữ nguyên string path
                            }
                        }
                        layerSetupHelper.setFeaturesMap(nested)

                        Log.d("PhotographFragment", "Features map to set: $nested")
                        // Set background nếu có
                        it.backgroundId?.let { bg ->
                            layerSetupHelper.setOutfitJsonWithBackground(
                                layerSetupHelper.getFullOutfitJson(backgroundId = bg)
                            )
                        }
                    }
                }
            }
        }
    }


    private fun onPhotoSelected(photo: OutfitEntity) {
        val nested = mutableMapOf<String, MutableMap<String, String>>()
        photo.features.forEach { (k, v) ->
            val parts = k.split('.')
            if (parts.size == 2) {
                val char = parts[0]
                val layer = parts[1]
                nested.getOrPut(char) { mutableMapOf() }[layer] = v // giữ nguyên string path
            }
        }
        layerSetupHelper.setFeaturesMap(nested)

        photo.backgroundId?.let { bg ->
            layerSetupHelper.setOutfitJsonWithBackground(
                layerSetupHelper.getFullOutfitJson(backgroundId = bg)
            )
        }
    }

    private fun saveCurrentOutfit() {
        layerSetupHelper.saveCurrentOutfitToGallery { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                val features = layerSetupHelper.getFeaturesAsStringMap()
                val bgId = getCurrentBgId()

                if (isFromGallery && outfitId != null) {
                    dao.getOutfitById(outfitId!!)?.let {
                        dao.update(it.copy(
                            features = features,
                            thumbnailPath = uri?.toString(),
                            backgroundId = bgId
                        ))
                        Log.d("PhotographFragment", "✅ Updated existing outfit ${it.id}")
                    }
                } else {
                    val newOutfit = OutfitEntity(
                        features = features,
                        backgroundId = bgId,
                        thumbnailPath = uri?.toString()
                    )
                    dao.insert(newOutfit)
                    Log.d("PhotographFragment", "✅ Created new outfit")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }

    private fun getCurrentBgId(): String? {
        return try {
            val obj = org.json.JSONObject(layerSetupHelper.getOutfitJson())
            obj.optString("backgroundId", null)
        } catch (e: Exception) { null }
    }
}

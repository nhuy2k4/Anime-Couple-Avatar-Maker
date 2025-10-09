package com.app.base.ui.photograph

import android.graphics.Bitmap
import android.net.Uri
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

    override fun initView() {
        binding.rcvPhotographs.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
        }
    }

    override fun initListener() {
        binding.btnBack.setOnClickListener {
            val action = PhotographFragmentDirections
                .actionPhotographFragmentToCategoryFragment(
                    outfitId = outfitId ?: -1L
                )
            findNavController().navigate(action)
        }

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
    outfitId = args.outfitId

    loadOutfitById(outfitId!!)
    }

    private fun loadOutfitById(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val outfit = AppDatabase.getInstance(requireContext()).outfitDao().getOutfitById(id)
            Log.d("PhotographFragment", "Loaded outfit = $outfit")

            withContext(Dispatchers.Main) {
                layerSetupHelper.setupInitialLayers(listOf("male", "female"),
                    offsetsX = mapOf(
                        "male" to 30f,     // căn trái
                        "female" to -10f)) {
                    outfit?.let {
                        // convert flat features map to nested
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

                        // if backgroundId exists, set via existing JSON API to keep background drawable behavior
                        it.backgroundId?.toIntOrNull()?.let { bg ->
                            // Ensure the backgroundId is included when calling setOutfitJsonWithBackground
                            layerSetupHelper.setOutfitJsonWithBackground(layerSetupHelper.getFullOutfitJson(backgroundId = bg))
                        }
                    }
                }
            }
        }
    }


    private fun onPhotoSelected(photo: OutfitEntity) {
        // 1️⃣ Update LayerSetupHelper from photo entity
        val nested = mutableMapOf<String, MutableMap<String, Int>>()
        photo.features.forEach { (k, v) ->
            val parts = k.split('.')
            if (parts.size == 2) {
                val char = parts[0]
                val layer = parts[1]
                val mapForChar = nested.getOrPut(char) { mutableMapOf() }
                mapForChar[layer] = v.toIntOrNull() ?: 0
            }
        }
        layerSetupHelper.setFeaturesMap(nested)
        photo.backgroundId?.toIntOrNull()?.let { bg ->
            // pass the explicit backgroundId so the helper can set the drawable
            layerSetupHelper.setOutfitJsonWithBackground(layerSetupHelper.getFullOutfitJson(backgroundId = bg))

            // Persist the chosen backgroundId into DB for this outfit
            outfitId?.let { id ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                    val existing = dao.getOutfitById(id)
                    existing?.let {
                        val featuresFlat = layerSetupHelper.getFeaturesAsStringMap()
                        // store both features and backgroundId so DB reflects selection
                        dao.update(
                            it.copy(
                                features = featuresFlat,
                                backgroundId = photo.backgroundId // keep as String
                            )
                        )
                        Log.d("PhotographFragment", "Saved backgroundId=${photo.backgroundId} to DB for outfit $id")
                    }
                }
            }
        }
    }


    private fun saveCurrentOutfit() {
        layerSetupHelper.saveCurrentOutfitToGallery { uri ->
            updateOutfitInDb(uri) {
                Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }
    private fun updateOutfitInDb(uri: Uri? = null, onComplete: (() -> Unit)? = null) {
        val featuresFlat = layerSetupHelper.getFeaturesAsStringMap()
        outfitId?.let { id ->  // đảm bảo id không null
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                val outfit = dao.getOutfitById(id)
                outfit?.let {
                    // also persist current backgroundId from layerSetupHelper
                    val currentJson = layerSetupHelper.getOutfitJson()
                    val bgId = try {
                        org.json.JSONObject(currentJson).optInt("backgroundId", 0)
                    } catch (e: Exception) { 0 }
                    val bgStr: String? = if (bgId != 0) bgId.toString() else it.backgroundId
                    dao.update(it.copy(features = featuresFlat, thumbnailPath = uri?.toString(), backgroundId = bgStr))
                }
                withContext(Dispatchers.Main) { onComplete?.invoke() }
            }
        }
    }


    private fun saveBitmapToGallery(bitmap: Bitmap, onDone: (Uri?) -> Unit) {
        layerSetupHelper.saveCurrentOutfitToGallery { uri ->
            updateOutfitInDb(uri) {
                Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.homeFragment)
            }
        }

    }
}

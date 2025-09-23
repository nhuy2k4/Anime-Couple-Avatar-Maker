package com.app.base.ui.photograph

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.base.R
import com.app.base.core.layer.LayerSetupHelper
import com.app.base.core.utils.PhotoCommitHelper
import com.app.base.core.utils.PhotoSaver
import com.app.base.databinding.FragmentPhotographBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.data.model.PhotographItem
import com.brally.mobile.utils.collectLatestFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.navigate

class PhotographFragment : BaseFragment<FragmentPhotographBinding, PhotographViewModel>() {

    private var selectedBackgroundId: Int? = null
    private val photographViewModel by viewModel<PhotographViewModel>()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private val photoAdapter by lazy { PhotographAdapter(onPhotoTapped = ::onPhotoSelected) }
    private lateinit var layerSetupHelper: LayerSetupHelper
    private var tempOutfitJson: String = "{}"

    override fun initView() {
        binding.rcvPhotographs.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
        }
    }

    override fun initListener() {
        binding.btnBack.setOnClickListener {
            navigate(R.id.categoryFragment)
        }

        binding.btnSave.setOnClickListener {
            val outfitJson = tempOutfitJson
            PhotoCommitHelper.saveAndCommit(binding.photoContainer, outfitJson, mainViewModel) {
                Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show()
                navigate(R.id.homeFragment)
            }
        }

        binding.btnDownload.setOnClickListener {
            val backgroundIdToSave = selectedBackgroundId ?: R.drawable.photo1
            val outfitJson = layerSetupHelper.getOutfitJsonWithBackground(backgroundIdToSave)
            PhotoSaver.savePhoto(
                binding.photoContainer,
                outfitJson,
                mainViewModel,
                onSuccess = {
                    Toast.makeText(requireContext(), "Outfit downloaded!", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Toast.makeText(requireContext(), "Error downloading outfit", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    private fun loadViewModelData() {
        photographViewModel.loadInitialData()
    }

    private fun observeViewModel() {
        collectLatestFlow(photographViewModel.photos) { list: List<PhotographItem> ->
            if (list.isNotEmpty()) photoAdapter.setPhotos(list)
            else photoAdapter.clearPhotos()
        }
    }

    private fun onPhotoSelected(photo: PhotographItem) {
        binding.photoEditorView.background = ContextCompat.getDrawable(requireContext(), photo.iconResId)
        tempOutfitJson = layerSetupHelper.getOutfitJsonWithBackground(photo.iconResId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layerSetupHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())

        val defaultBackgroundRes = R.drawable.bg_gradient
        val photoUriFromGallery = arguments?.getString("photo_uri")?.let { Uri.parse(it) }
        val outfitJsonFromGallery = photoUriFromGallery?.let { PhotoCommitHelper.loadOutfitJson(it, requireContext()) }

        layerSetupHelper.setupInitialLayers {
            when {
                // Gallery + outfit JSON đã lưu
                !outfitJsonFromGallery.isNullOrEmpty() -> {
                    layerSetupHelper.setOutfitJsonWithBackground(outfitJsonFromGallery)
                    loadBackground(photoUriFromGallery!!)
                }

                // Category (đã có currentOutfit)
                mainViewModel.currentOutfit.value != null -> {
                    val existingOutfit = mainViewModel.currentOutfit.value!!
                    layerSetupHelper.setOutfitJsonWithBackground(existingOutfit.json)
                    if (binding.photoEditorView.background == null) {
                        binding.photoEditorView.setBackgroundResource(defaultBackgroundRes)
                    }
                }

                // Fallback
                else -> {
                    val newOutfitJson = "{}"
                    mainViewModel.createNewOutfit(newOutfitJson)
                    layerSetupHelper.setOutfitJsonWithBackground(newOutfitJson)
                    binding.photoEditorView.setBackgroundResource(defaultBackgroundRes)
                }
            }
        }

        logCurrentOutfits()
    }

    private fun loadBackground(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val drawable = Drawable.createFromStream(inputStream, uri.toString())
            binding.photoEditorView.background = drawable
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            binding.photoEditorView.setBackgroundResource(R.drawable.bg_gradient)
        }
    }

    private fun logCurrentOutfits() {
        val outfits = mainViewModel.outfitListData.value
        Log.d("OutfitListDebug", "=== Current outfits ===")
        outfits.forEachIndexed { index, outfit ->
            Log.d("OutfitListDebug", "$index: id=${outfit.id}, json=${outfit.json}, uri=${outfit.uri}")
        }
        Log.d("OutfitListDebug", "=== End of list ===")
    }
}

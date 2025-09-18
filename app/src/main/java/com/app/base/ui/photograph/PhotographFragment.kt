package com.app.base.ui.photograph

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.app.base.core.layer.LayerSetupHelper
import com.app.base.databinding.FragmentPhotographBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.data.model.PhotographItem
import com.brally.mobile.utils.collectLatestFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import com.app.base.ui.main.MainViewModel
import kotlinx.coroutines.launch

class PhotographFragment : BaseFragment<FragmentPhotographBinding, PhotographViewModel>() {
    private var selectedBackgroundId: Int? = null

    private val photographViewModel by viewModel<PhotographViewModel>()
    private val mainViewModel by activityViewModel<MainViewModel>()

    private val photoAdapter by lazy {
        PhotographAdapter(onPhotoTapped = ::onPhotoSelected)
    }

    private lateinit var layerSetupHelper: LayerSetupHelper

    override fun initView() {
        // Setup RecyclerView for background selection
        binding.rcvPhotographs.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
        }

        // Setup LayerSetupHelper với 2 nhân vật
        layerSetupHelper = LayerSetupHelper(
            binding.photoContainer,
            binding.photoEditorView,
            requireContext()
        )

        layerSetupHelper.setupInitialLayers {
            val outfitJson = mainViewModel.currentOutfit.value
            if (outfitJson.isNotEmpty()) {
                layerSetupHelper.setOutfitJson(outfitJson)
            }
        }

    }
    fun onUserChangedOutfit(json: String) {
        mainViewModel.updateCurrentOutfit(json)
    }
    override fun initListener() {
        // Back button
        binding.btnBack.setOnClickListener { popBackStack() }

        // Save / Download buttons
        binding.btnSave.setOnClickListener { savePhoto("save") }
        binding.btnDownload.setOnClickListener { savePhoto("download") }
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    // ---------------------- ViewModel ----------------------
    private fun loadViewModelData() {
        photographViewModel.loadInitialData()
    }

    private fun observeViewModel() {
        collectLatestFlow(photographViewModel.photos) { list: List<PhotographItem> ->
            if (list.isNotEmpty()) photoAdapter.setPhotos(list)
            else photoAdapter.clearPhotos()
        }
    }

    // ---------------------- Events ----------------------
    private fun onPhotoSelected(photo: PhotographItem) {
        val drawable = ContextCompat.getDrawable(requireContext(), photo.iconResId)
        binding.photoEditorView.background = drawable
        selectedBackgroundId = photo.iconResId

        // Cập nhật outfit hiện tại với background
        val outfitJson = layerSetupHelper.getOutfitJsonWithBackground(selectedBackgroundId)
        mainViewModel.updateCurrentOutfit(outfitJson)
    }

    private fun savePhoto(name: String) {
        lifecycleScope.launch {
            try {
                // Suspend function trả về Bitmap
                val bitmap: Bitmap = layerSetupHelper.photoEditor.saveAsBitmap()
                val file = mainViewModel.saveBitmapToCached(bitmap)
                file?.let {
                    Toast.makeText(requireContext(), "$name saved to cache: ${it.path}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error saving $name", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

}

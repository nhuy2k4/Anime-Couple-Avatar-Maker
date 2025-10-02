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
                layerSetupHelper.setupInitialLayers {
                    outfit?.let {
                        // 1️⃣ Nếu outfit có backgroundId trong JSON thì set
                        val outfitJson = it.outfitJson
                        val jsonObj = org.json.JSONObject(outfitJson)
                        val bgId = if (jsonObj.has("backgroundId")) {
                            jsonObj.getInt("backgroundId")
                        } else {
                            null
                        }

                        if (bgId != null) {
                            layerSetupHelper.setOutfitJsonWithBackground(outfitJson)
                        } else {
                            // Chỉ set features, không dùng default background
                            layerSetupHelper.setOutfitJson(outfitJson)
                        }
                    }
                }
            }
        }
    }


    private fun onPhotoSelected(photo: OutfitEntity) {
        // 1️⃣ Update LayerSetupHelper
        layerSetupHelper.setOutfitJsonWithBackground(photo.outfitJson)

        // 2️⃣ Update DB ngay lập tức
        outfitId?.let { id ->
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                val existing = dao.getOutfitById(id)
                existing?.let {
                    // Cập nhật JSON mới, giữ URI cũ
                    dao.update(
                        it.copy(
                            outfitJson = layerSetupHelper.getOutfitJson()
                            // backgroundUri vẫn giữ nguyên
                        )
                    )
                    Log.d("PhotographFragment", "Updated DB backgroundId ngay khi chọn photo")
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
        val outfitJson = layerSetupHelper.getFullOutfitJson(backgroundUri = uri)
        outfitId?.let { id ->  // đảm bảo id không null
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                val outfit = dao.getOutfitById(id)
                outfit?.let {
                    dao.update(it.copy(outfitJson = outfitJson, backgroundUri = uri?.toString()))
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

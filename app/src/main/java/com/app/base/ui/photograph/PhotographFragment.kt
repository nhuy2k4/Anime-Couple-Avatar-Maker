package com.app.base.ui.photograph

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.app.base.R
import com.app.base.core.layer.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.database.OutfitEntity
import com.app.base.databinding.FragmentPhotographBinding
import com.brally.mobile.base.activity.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.OutputStream

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

        if (outfitId == -1L) {
            // Outfit mới → setup initial
            layerSetupHelper.setupInitialLayers {
                layerSetupHelper.setOutfitJsonWithBackground("{}")
            }
        } else {
            // Load outfit từ DB
            loadOutfitById(outfitId!!)
        }
    }

    private fun loadOutfitById(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val outfit = AppDatabase.getInstance(requireContext()).outfitDao().getOutfitById(id)
            Log.d("PhotographFragment", "Loaded outfit = $outfit")

            withContext(Dispatchers.Main) {
                val uri = outfit?.backgroundUri?.let { Uri.parse(it) }
                layerSetupHelper.setupInitialLayers {
                    layerSetupHelper.setOutfitJsonWithBackground(outfit?.outfitJson ?: "{}", uri)
                }
            }
        }
    }

    private fun onPhotoSelected(photo: OutfitEntity) {
        val uri = photo.backgroundUri?.let { Uri.parse(it) }
        layerSetupHelper.setOutfitJsonWithBackground(photo.outfitJson, uri)
    }

    private fun saveCurrentOutfit() {
        val outfitJson = layerSetupHelper.getOutfitJson()
        val bitmap = layerSetupHelper.renderOutfitBitmap()

        saveBitmapToGallery(bitmap) { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                outfitId?.let { id ->
                    val dao = AppDatabase.getInstance(requireContext()).outfitDao()
                    val existing = dao.getOutfitById(id)
                    existing?.let {
                        dao.update(
                            it.copy(
                                outfitJson = outfitJson,
                                backgroundUri = uri?.toString()
                            )
                        )
                        Log.d("PhotographFragment", "Updated Outfit id=$id")
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Outfit saved!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, onDone: (Uri?) -> Unit) {
        val resolver = requireContext().contentResolver
        try {
            val fileName = "outfit_${System.currentTimeMillis()}.png"
            val values = android.content.ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out: OutputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            onDone(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            onDone(null)
        }
    }
}

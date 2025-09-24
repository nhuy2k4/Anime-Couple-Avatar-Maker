package com.app.base.ui.gallery

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import com.app.base.databinding.FragmentGalleryBinding
import com.app.base.ui.photograph.PhotographFragmentDirections
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.utils.singleClick
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    private val galleryViewModel: GalleryViewModel by viewModel()
    private lateinit var galleryAdapter: GalleryAdapter

    override fun initView() {
        galleryAdapter = GalleryAdapter(
            onPhotoClicked = { outfit ->
                // Khi click photo → truyền outfitId
                val action = GalleryFragmentDirections.actionGalleryFragmentToPhotographFragment(
                    outfitId = outfit.id
                )
                findNavController().navigate(action)
            },
            onDeleteClicked = { outfit ->
                galleryViewModel.deletePhoto(requireContext(), outfit)
            }
        )

        binding.rcvGallery.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = galleryAdapter
        }
    }

    override fun initListener() {
        binding.btnHome.singleClick { popBackStack() }
    }

    override fun initData() {
        // Load outfits từ Room
        galleryViewModel.loadSavedOutfits(requireContext())

        lifecycleScope.launch {
            galleryViewModel.photos.collectLatest { outfits ->
                if (outfits.isNotEmpty()) galleryAdapter.setPhotos(outfits)
                else galleryAdapter.clearPhotos()
            }
        }
    }
}

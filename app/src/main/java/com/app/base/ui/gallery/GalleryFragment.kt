package com.app.base.ui.gallery

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.app.base.R
import com.app.base.databinding.FragmentGalleryBinding
import com.app.base.ui.main.MainViewModel
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.utils.singleClick
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    private val galleryViewModel: GalleryViewModel by viewModels()
    private val mainViewModel by activityViewModel<MainViewModel>()
    private lateinit var galleryAdapter: GalleryAdapter

    override fun initView() {
        galleryAdapter = GalleryAdapter(
            onPhotoClicked = { item ->
                item.outfitJson?.let { json ->
                    mainViewModel.commitOutfitWithBitmap(
                        json = json,
                        bitmapFile = null,
                        uri = item.imageUri
                    )
                }
                val action = GalleryFragmentDirections.actionGalleryFragmentToPhotographFragment(
                    photoUri = item.imageUri.toString(),
                    outfitJson = item.outfitJson.toString()
                )
                findNavController().navigate(action)

            },
            onDeleteClicked = { item ->
                galleryViewModel.deletePhoto(requireContext(), item)
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
        galleryViewModel.loadSavedPhotos(requireContext(), mainViewModel)

        lifecycleScope.launch {
            galleryViewModel.photos.collectLatest { items ->
                galleryAdapter.setPhotos(items)
            }
        }
    }
}


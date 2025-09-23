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
    private val galleryAdapter by lazy {
        GalleryAdapter { item ->
            Log.d("GalleryFragment", "User selected photo uri=${item.imageUri}, outfitJson=${item.outfitJson}")

            item.outfitJson?.let { json ->
                // Đồng bộ outfit + URI vào MainViewModel
                mainViewModel.commitOutfitWithBitmap(
                    json = json,
                    bitmapFile = null,
                    uri = item.imageUri,
                    mediaId = ContentUris.parseId(item.imageUri)
                )
            }

            val bundle = Bundle().apply {
                putString("outfit_json", item.outfitJson)
                putString("photo_uri", item.imageUri.toString())
            }
            findNavController().navigate(R.id.photographFragment, bundle)
        }
    }


    override fun initView() {
        binding.rcvGallery.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = galleryAdapter
        }
    }

    override fun initListener() {
        binding.btnHome.singleClick {
            popBackStack()
        }

    }

    override fun initData() {
        // Gọi load ảnh, truyền context
        galleryViewModel.loadSavedPhotos(requireContext(), mainViewModel )

        lifecycleScope.launch {
            galleryViewModel.photos.collectLatest { items ->
                galleryAdapter.setPhotos(items)
            }
        }
    }
}

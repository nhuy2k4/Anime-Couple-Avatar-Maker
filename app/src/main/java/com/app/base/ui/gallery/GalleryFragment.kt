package com.app.base.ui.gallery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import com.app.base.R
import com.app.base.databinding.FragmentGalleryBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.utils.singleClick
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    private val galleryViewModel: GalleryViewModel by viewModels()

    private val galleryAdapter by lazy {
        GalleryAdapter { item ->
            val bundle = Bundle().apply {
                putString("outfit_json", item.outfitJson)
                putString("photo_uri", item.imageUri.toString()) // dùng Uri thay vì file
            }
            findNavController().navigate(
                R.id.photographFragment,
                bundle
            )
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
        galleryViewModel.loadSavedPhotos(requireContext())

        lifecycleScope.launch {
            galleryViewModel.photos.collectLatest { items ->
                galleryAdapter.setPhotos(items)
            }
        }
    }
}

package com.app.base.ui.gallery

import androidx.recyclerview.widget.GridLayoutManager
import com.app.base.R
import com.app.base.databinding.FragmentGalleryBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.data.model.GalleryItem
import com.brally.mobile.utils.collectLatestFlow
import com.brally.mobile.utils.singleClick
import org.koin.androidx.viewmodel.ext.android.viewModel

class GalleryFragment : BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    private val galleryViewModel by viewModel<GalleryViewModel>()
    private val galleryAdapter by lazy { GalleryAdapter(onItemClicked = ::onPhotoSelected) }

    override fun initView() {
        // Setup RecyclerView
        binding.rcvGallery.apply {
            layoutManager = GridLayoutManager(context, 2) // Grid 3 cột
            adapter = galleryAdapter
        }
    }

    override fun initListener() {
        binding.btnHome.singleClick {
            popBackStack()
        }
    }

    override fun initData() {
        loadViewModelData()
        observeViewModel()
    }

    // ---------------------- ViewModel ----------------------
    private fun loadViewModelData() {
        galleryViewModel.loadInitialData()
    }

    private fun observeViewModel() {
        collectLatestFlow(galleryViewModel.photos) { list ->
            if (list.isNotEmpty()) {
                galleryAdapter.setPhotos(list)
            } else {
                galleryAdapter.clearPhotos()
            }
        }
    }

    // ---------------------- Events ----------------------
    private fun onPhotoSelected(photo: GalleryItem) {
        // Ví dụ: set ảnh vào PhotoEditorView hoặc xử lý sự kiện khác
//        val editorView = requireActivity().findViewById<PhotoEditorView>(R.id.photoEditorView)
//        val drawable = requireContext().getDrawable(photo.iconResId)
//        editorView.background = drawable
    }
}

package com.app.base.ui.photograph

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.base.R
import com.app.base.databinding.FragmentPhotographBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.data.model.PhotographItem
import com.brally.mobile.utils.collectLatestFlow
import org.koin.androidx.viewmodel.ext.android.viewModel
import ja.burhanrashid52.photoeditor.PhotoEditorView
class PhotographFragment : BaseFragment<FragmentPhotographBinding, PhotographViewModel>() {

    private val photographViewModel by viewModel<PhotographViewModel>()
    private val photoAdapter by lazy { PhotographAdapter(onPhotoTapped = ::onPhotoSelected) }

    override fun initView() {
        // Setup RecyclerView
        binding.rcvPhotographs.apply {
            layoutManager = GridLayoutManager(context, 3) // Grid 3 cột
            adapter = photoAdapter
        }
    }

    override fun initListener() {
        // TODO: nếu có nút back/home/reset thì setup ở đây
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
            if (list.isNotEmpty()) {
                photoAdapter.setPhotos(list)
            } else {
                photoAdapter.clearPhotos()
            }
        }
    }


    // ---------------------- Events ----------------------
    private fun onPhotoSelected(photo: PhotographItem) {
        // Lấy PhotoEditorView từ Activity hoặc SharedViewModel
        val editorView = requireActivity()
            .findViewById<PhotoEditorView>(R.id.photoEditorView)

        val drawable = ContextCompat.getDrawable(requireContext(), photo.iconResId)

        // Set làm background
        editorView.background = drawable
    }
}

package com.app.base.ui.photograph

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.ItemPhotographBinding
import com.brally.mobile.data.model.PhotographItem
import com.brally.mobile.utils.singleClick

/**
 * Adapter cho RecyclerView hiển thị danh sách ảnh trong PhotographFragment
 */
class PhotographAdapter(
    private val onPhotoTapped: ((PhotographItem) -> Unit)? = null
) : RecyclerView.Adapter<PhotographAdapter.ViewHolder>() {

    private val photos = mutableListOf<PhotographItem>()

    fun setPhotos(newPhotos: List<PhotographItem>) {
        photos.clear()
        photos.addAll(newPhotos.distinctBy { it.iconResId })
        notifyDataSetChanged()
    }

    fun clearPhotos() {
        photos.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPhotographBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < photos.size) {
            holder.bind(photos[position])
        }
    }

    override fun getItemCount(): Int = photos.size

    inner class ViewHolder(
        private val binding: ItemPhotographBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PhotographItem) {
            // Load ảnh drawable
            binding.imvPhoto.setImageResource(item.iconResId)

            binding.root.singleClick {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onPhotoTapped?.invoke(item)
                }
            }
        }
    }
}

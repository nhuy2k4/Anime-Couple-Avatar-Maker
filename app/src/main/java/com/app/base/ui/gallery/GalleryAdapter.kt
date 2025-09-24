package com.app.base.ui.gallery

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.databinding.ItemGalleryBinding
import com.brally.mobile.data.model.GalleryItem

class GalleryAdapter(
    private val onPhotoClicked: (GalleryItem) -> Unit,
    private val onDeleteClicked: (GalleryItem) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val items = mutableListOf<GalleryItem>()

    fun setPhotos(list: List<GalleryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(private val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GalleryItem) {
            binding.imvGallery.setImageURI(item.imageUri)

            binding.root.setOnClickListener { onPhotoClicked(item) }
            binding.btnDel.setOnClickListener { onDeleteClicked(item) }
        }
    }
}



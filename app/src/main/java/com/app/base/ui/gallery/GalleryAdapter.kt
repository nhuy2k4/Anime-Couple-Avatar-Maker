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
    private val onClick: (GalleryItem) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>() {

    private val items = mutableListOf<GalleryItem>()

    fun setPhotos(list: List<GalleryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class PhotoViewHolder(val binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GalleryItem) {
            // Load ảnh từ Uri
            binding.imvGallery.setImageURI(item.imageUri)

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}



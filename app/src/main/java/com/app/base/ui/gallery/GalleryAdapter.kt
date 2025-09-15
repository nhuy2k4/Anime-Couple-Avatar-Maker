package com.app.base.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.ItemGalleryBinding
import com.brally.mobile.data.model.GalleryItem

class GalleryAdapter(
    private val onItemClicked: (GalleryItem) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private val items = mutableListOf<GalleryItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setPhotos(list: List<GalleryItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun clearPhotos() {
        items.clear()
        notifyDataSetChanged()
    }

    inner class GalleryViewHolder(private val binding: ItemGalleryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GalleryItem) {
            binding.imageView.setImageResource(item.iconResId)
            binding.root.setOnClickListener { onItemClicked(item) }
        }
    }
}

package com.app.base.core.layer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.ItemArtBinding

class LayerItemAdapter(
    private val items: MutableList<Int>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<LayerItemAdapter.LayerViewHolder>() {

    private var selectedResId: Int? = null  // lưu resId đang chọn

    inner class LayerViewHolder(val binding: ItemArtBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(resId: Int) {
            // Set hình ảnh cho ShapeableImageView
            binding.imvArt.setImageResource(resId)

            // Hiển thị dấu tích nếu đang chọn
            binding.checkView.visibility = if (resId == selectedResId) View.VISIBLE else View.GONE

            // Set click listener
            binding.root.setOnClickListener {
                selectedResId = resId
                notifyDataSetChanged()  // refresh dấu tích
                onItemClick(resId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        val binding = ItemArtBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Int>, selectedRes: Int? = null) {
        items.clear()
        items.addAll(newItems)
        selectedResId = selectedRes
        notifyDataSetChanged()
    }
}
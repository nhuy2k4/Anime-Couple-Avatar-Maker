package com.app.base.core.layer

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.ItemArtBinding

// Display item can be either a drawable resource id or an asset path
data class DisplayItem(val resId: Int? = null, val assetPath: String? = null)

class LayerItemAdapter(
    private val items: MutableList<DisplayItem>,
    private val onItemClick: (DisplayItem) -> Unit
) : RecyclerView.Adapter<LayerItemAdapter.LayerViewHolder>() {

    private var selectedIdentifier: String? = null  // identifier: resId.toString() or assetPath

    inner class LayerViewHolder(val binding: ItemArtBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DisplayItem) {
            // Load image either from resource or from assets
            if (item.resId != null) {
                binding.imvArt.setImageResource(item.resId)
            } else if (item.assetPath != null) {
                try {
                    val istr = binding.imvArt.context.assets.open(item.assetPath)
                    val bmp = BitmapFactory.decodeStream(istr)
                    binding.imvArt.setImageBitmap(bmp)
                    istr.close()
                } catch (e: Exception) {
                    binding.imvArt.setImageDrawable(null)
                }
            } else {
                binding.imvArt.setImageDrawable(null)
            }

            // Identifier
            val id = item.assetPath ?: item.resId?.toString()
            binding.checkView.visibility = if (id == selectedIdentifier) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                selectedIdentifier = id
                notifyDataSetChanged()
                onItemClick(item)
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

    fun updateData(newItems: List<DisplayItem>, selectedId: String? = null) {
        items.clear()
        items.addAll(newItems)
        selectedIdentifier = selectedId
        notifyDataSetChanged()
    }
}
package com.app.base.ui.category

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class LayerItemAdapter(
    private val items: MutableList<Int>,           // list drawable resource
    private val onItemClick: (Int) -> Unit // callback khi chọn
) : RecyclerView.Adapter<LayerItemAdapter.LayerViewHolder>() {

    inner class LayerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        fun bind(resId: Int) {
            imageView.setImageResource(resId)
            imageView.setOnClickListener { onItemClick(resId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(200, 200)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return LayerViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        holder.bind(items[position])
    }
    fun updateData(newItems: List<Int>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = items.size
}

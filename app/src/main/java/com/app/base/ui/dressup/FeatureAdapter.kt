package com.app.base.ui.dressup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.databinding.ItemFeatureBinding
import com.app.base.model.FeatureItem
import com.app.base.utils.AssetImageLoader

class FeatureAdapter(
    private val onFeatureSelected: ((FeatureItem) -> Unit)? = null
) : RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {

    private val features = mutableListOf<FeatureItem>()

    fun setFeatures(newFeatures: List<FeatureItem>) {
        features.clear()
        features.addAll(newFeatures)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(features[position])
    }

    override fun getItemCount(): Int = features.size

    inner class ViewHolder(private val binding: ItemFeatureBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(feature: FeatureItem) {
            // Load image từ assets
            android.util.Log.d("FeatureAdapter", "Binding feature: ${feature.id}, image=${feature.image}")
            AssetImageLoader.loadImage(binding.imageFeature, binding.root.context, feature.image)

            // Kiểm tra unlock
            val isUnlocked = feature.unlockCondition == "free" // hoặc logic khác
            binding.root.alpha = if (isUnlocked) 1f else 0.4f

            binding.root.setOnClickListener {
                if (isUnlocked) onFeatureSelected?.invoke(feature)
            }
        }
    }
}

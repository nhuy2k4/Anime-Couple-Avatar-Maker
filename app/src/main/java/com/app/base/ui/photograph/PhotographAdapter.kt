package com.app.base.ui.photograph

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.database.OutfitEntity
import com.app.base.databinding.ItemPhotographBinding
import org.json.JSONObject

/**
 * Adapter cho RecyclerView hiển thị danh sách outfit từ Room DB
 */
class PhotographAdapter(
    private val onPhotoTapped: ((OutfitEntity) -> Unit)? = null
) : RecyclerView.Adapter<PhotographAdapter.ViewHolder>() {

    private val outfits = mutableListOf<OutfitEntity>()

    // 2 item mặc định
    private val defaultItems = listOf(
        OutfitEntity(
            outfitJson = """{"backgroundId": ${R.drawable.bg_gradient}}""",
            backgroundUri = null
        ),
        OutfitEntity(
            outfitJson = """{"backgroundId": ${R.drawable.photo1}}""",
            backgroundUri = null
        ),
        OutfitEntity(
            outfitJson = """{"backgroundId": ${R.drawable.photo2}}""",
            backgroundUri = null
        )
    )

    fun setPhotos(newOutfits: List<OutfitEntity>) {
        outfits.clear()
        // Gộp 2 item mặc định + outfit từ Room
        outfits.addAll(defaultItems)
        notifyDataSetChanged()
    }

    fun clearPhotos() {
        outfits.clear()
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
        if (position < outfits.size) {
            holder.bind(outfits[position])
        }
    }

    override fun getItemCount(): Int = outfits.size

    inner class ViewHolder(private val binding: ItemPhotographBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OutfitEntity) {
            val bgUri = item.backgroundUri?.let { Uri.parse(it) }
            if (bgUri != null) {
                binding.imvPhoto.setImageURI(bgUri) // Hiển thị đúng bitmap đã save
            } else {
                val bgId = JSONObject(item.outfitJson).optInt("backgroundId", R.drawable.bg_gradient)
                binding.imvPhoto.setImageResource(bgId)
            }


            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onPhotoTapped?.invoke(item)
                }
            }
        }
    }
}


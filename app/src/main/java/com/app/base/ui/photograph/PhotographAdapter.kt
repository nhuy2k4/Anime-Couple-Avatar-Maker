package com.app.base.ui.photograph

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.database.entity.OutfitEntity
import com.app.base.databinding.ItemPhotographBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotographAdapter(
    private val context: Context,
    private val onBackgroundTapped: ((OutfitEntity) -> Unit)? = null
) : RecyclerView.Adapter<PhotographAdapter.ViewHolder>() {

    private val backgrounds = mutableListOf<OutfitEntity>()

    fun setBackgrounds(list: List<OutfitEntity>) {
        backgrounds.clear()
        backgrounds.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemPhotographBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(backgrounds[position])
    }

    override fun getItemCount(): Int = backgrounds.size

    inner class ViewHolder(private val binding: ItemPhotographBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OutfitEntity) {
            // backgroundId giờ là path trong assets
            val path = item.backgroundId
            if (!path.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val drawable = loadDrawableFromAssets(path)
                    binding.imvPhoto.setImageDrawable(drawable)
                }
            } else {
                binding.imvPhoto.setImageResource(android.R.color.darker_gray)
            }

            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onBackgroundTapped?.invoke(item)
                }
            }
        }

        private suspend fun loadDrawableFromAssets(path: String): Drawable? {
            return withContext(Dispatchers.IO) {
                try {
                    context.assets.open(path).use { stream ->
                        Drawable.createFromStream(stream, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }
}

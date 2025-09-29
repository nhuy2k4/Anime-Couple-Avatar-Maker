//package com.app.base.ui.gallery
//
//import android.net.Uri
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.app.base.R
//import com.app.base.database.OutfitEntity
//import com.app.base.databinding.ItemGalleryBinding
//import org.json.JSONObject
//
//class GalleryAdapter(
//    private val onPhotoClicked: (OutfitEntity) -> Unit,
//    private val onDeleteClicked: ((OutfitEntity) -> Unit)? = null
//) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
//
//    private val items = mutableListOf<OutfitEntity>()
//
//    fun setPhotos(list: List<OutfitEntity>) {
//        items.clear()
//        items.addAll(list)
//        notifyDataSetChanged()
//    }
//
//    fun clearPhotos() {
//        items.clear()
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ViewHolder(binding)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(items[position], position)
//    }
//
//    inner class ViewHolder(private val binding: ItemGalleryBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: OutfitEntity, position: Int) {
//            // Load background ưu tiên URI, fallback backgroundId
//            val bgUri = item.backgroundUri?.let { Uri.parse(it) }
//            if (bgUri != null) {
//                binding.imvGallery.setImageURI(bgUri)
//            } else {
//                val bgId = try {
//                    JSONObject(item.outfitJson).optInt("backgroundId", R.drawable.bg_gradient)
//                } catch (e: Exception) {
//                    R.drawable.bg_gradient
//                }
//                binding.imvGallery.setImageResource(bgId)
//            }
//
//            // Set tên ảnh: Photo 1, 2, 3 ...
//            binding.tvGallery.text = "Photo ${position + 1}"
//
//            binding.root.setOnClickListener { onPhotoClicked(item) }
//            binding.btnDel.setOnClickListener { onDeleteClicked?.invoke(item) }
//        }
//    }
//}

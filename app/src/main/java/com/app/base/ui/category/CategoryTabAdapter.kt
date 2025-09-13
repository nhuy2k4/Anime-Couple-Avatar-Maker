package com.app.base.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.utils.singleClick
import com.app.base.R
import com.app.base.databinding.ItemCategoryTabBinding

/**
 * Adapter cho RecyclerView hiển thị danh sách categories
 * - Quản lý danh sách categories
 * - Xử lý click events
 * - Quản lý trạng thái selected
 */
class CategoryTabAdapter(
    private val onItemTapped: ((CategoryItem) -> Unit)? = null
) : RecyclerView.Adapter<CategoryTabAdapter.ViewHolder>() {

    // ========== PROPERTIES ==========
    private val categories = mutableListOf<CategoryItem>()
    private var selectedPosition = 0

    // ========== PUBLIC METHODS ==========

    /**
     * Cập nhật danh sách categories
     */
    fun setCategories(newCategories: List<CategoryItem>) {
        categories.clear()
        categories.addAll(newCategories.distinct())
        selectedPosition = if (categories.isNotEmpty()) 0 else -1  // Reset nếu không empty
        notifyDataSetChanged()
    }

    /**
     * Thêm categories mới
     */
    fun addCategories(newCategories: List<CategoryItem>) {
        val startPosition = categories.size
        categories.addAll(newCategories.distinct())
        notifyItemRangeInserted(startPosition, newCategories.size)
    }

    /**
     * Chọn category tại vị trí position
     */
    fun selectCategory(position: Int) {
        if (position == selectedPosition || position >= categories.size || position < 0) return

        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    /**
     * Lấy category được chọn hiện tại
     */
    fun getSelectedCategory(): CategoryItem? {
        return if (selectedPosition in 0 until categories.size) {
            categories[selectedPosition]
        } else null
    }

    /**
     * Lấy vị trí category được chọn
     */
    fun getSelectedPosition(): Int = selectedPosition

    /**
     * Clear tất cả categories
     */
    fun clearCategories() {
        categories.clear()
        selectedPosition = -1  // Invalid
        notifyDataSetChanged()
    }

    // ========== RECYCLERVIEW METHODS ==========

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryTabBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < categories.size) {
            holder.bind(categories[position], position == selectedPosition)
        }
    }

    override fun getItemCount(): Int = categories.size

    // ========== VIEWHOLDER ==========

    inner class ViewHolder(
        private val binding: ItemCategoryTabBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(category: CategoryItem, isSelected: Boolean) {
            // Set image and content description
            binding.imvCategory.setImageResource(category.iconResId)
            binding.imvCategory.contentDescription = category.value

            // Set background based on selection state
            updateBackground(isSelected)

            // Set click listener
            setupClickListener(category)
        }

        /**
         * Cập nhật background dựa trên trạng thái selected
         */
        private fun updateBackground(isSelected: Boolean) {
            val backgroundRes = if (isSelected) {
                R.drawable.ic_category_selected
            } else {
                R.drawable.ic_category_normal
            }
            binding.cardCategory.setBackgroundResource(backgroundRes)
        }

        /**
         * Setup click listener
         */
        private fun setupClickListener(category: CategoryItem) {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    selectCategory(adapterPosition)
                    onItemTapped?.invoke(category)
                }
            }
        }
    }
}
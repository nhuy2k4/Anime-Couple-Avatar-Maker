package com.app.base.ui.dressup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.databinding.ItemDressUpTabBinding
import com.brally.mobile.data.model.TabItem

/**
 * Adapter cho RecyclerView hiển thị danh sách tab trong DressUpFragment
 * - Quản lý danh sách tab
 * - Xử lý click events
 * - Quản lý trạng thái selected
 */
class TabAdapter(
    private val onItemTapped: ((TabItem) -> Unit)? = null
) : RecyclerView.Adapter<TabAdapter.ViewHolder>() {

    private val tabs = mutableListOf<TabItem>()
    private var selectedPosition = 0

    fun setTabs(newTabs: List<TabItem>) {
        tabs.clear()
        tabs.addAll(newTabs.distinctBy { it.id })
        selectedPosition = if (tabs.isNotEmpty()) 0 else -1
        notifyDataSetChanged()
    }

    fun addTabs(newTabs: List<TabItem>) {
        val startPosition = tabs.size
        tabs.addAll(newTabs.distinctBy { it.id })
        notifyItemRangeInserted(startPosition, newTabs.size)
    }

    fun selectTab(position: Int) {
        if (position == selectedPosition || position !in tabs.indices) return
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    fun getSelectedTab(): TabItem? = tabs.getOrNull(selectedPosition)

    fun getSelectedPosition(): Int = selectedPosition

    fun clearTabs() {
        tabs.clear()
        selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDressUpTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < tabs.size) {
            holder.bind(tabs[position], position == selectedPosition)
        }
    }

    override fun getItemCount(): Int = tabs.size

    inner class ViewHolder(private val binding: ItemDressUpTabBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tab: TabItem, isSelected: Boolean) {
            // Set tên và icon drawable
            binding.imageTab.setImageResource(tab.iconRes)

            updateBackground(isSelected)
            setupClickListener(tab)
        }

        private fun updateBackground(isSelected: Boolean) {
            val backgroundRes = if (isSelected) {
                R.drawable.ic_category_selected
            } else {
                R.drawable.ic_category_normal
            }
            binding.cardTab.setBackgroundResource(backgroundRes)
        }

        private fun setupClickListener(tab: TabItem) {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    selectTab(adapterPosition)
                    onItemTapped?.invoke(tab)
                }
            }
        }
    }
}

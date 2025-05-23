package com.language_onboard.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.braly.ads.R
import com.braly.ads.databinding.CommonItemLanguageBinding
import com.language_onboard.data.model.LanguageSelector
import com.language_onboard.utils.loadImage

class CommonLanguageAdapter(
    private val onItemClickListener: ((LanguageSelector) -> Unit)? = null
) :
    ListAdapter<LanguageSelector, CommonLanguageAdapter.ViewHolder>(DIFF_ITEM) {
    companion object {
        val DIFF_ITEM = object : DiffUtil.ItemCallback<LanguageSelector>() {
            override fun areItemsTheSame(
                oldItem: LanguageSelector,
                newItem: LanguageSelector
            ): Boolean {
                return oldItem.language.countryCode == newItem.language.countryCode
            }

            override fun areContentsTheSame(
                oldItem: LanguageSelector,
                newItem: LanguageSelector
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: CommonItemLanguageBinding,
        private val onItemClickListener: ((LanguageSelector) -> Unit)? = null
    ) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        private lateinit var item: LanguageSelector

        init {
            binding.root.setOnClickListener(this::onClick)
        }

        override fun onClick(p0: View?) {
            onItemClickListener?.invoke(item)
        }

        fun bindData(item: LanguageSelector) {
            this.item = item
            binding.ivLanguage.loadImage(item.language.imageRes)
            binding.tvLanguage.text = item.language.countryName
            binding.ivCheck.setImageResource(if (item.isCheck) R.drawable.ic_check else R.drawable.ic_uncheck)
            binding.root.background = if (item.isCheck) {
                ContextCompat.getDrawable(itemView.context, R.drawable.bg_language_selected)
            } else {
                ContextCompat.getDrawable(itemView.context, R.drawable.bg_sound_unselect)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            CommonItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onItemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(getItem(position))
    }
}

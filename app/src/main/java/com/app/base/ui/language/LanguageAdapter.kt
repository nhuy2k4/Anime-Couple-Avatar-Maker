package com.app.base.ui.language

import androidx.core.content.ContextCompat
import com.brally.mobile.base.R
import com.brally.mobile.base.adapter.BaseRecyclerViewAdapter
import com.brally.mobile.data.model.LanguageSelector
import com.app.base.databinding.ItemLanguageBinding

class LanguageAdapter : BaseRecyclerViewAdapter<LanguageSelector, ItemLanguageBinding>() {

    override fun bindData(binding: ItemLanguageBinding, item: LanguageSelector, position: Int) {
        binding.tvLanguage.text = item.language.language
        binding.imvLanguage.setImageResource(item.language.flag)
        binding.root.background = if (item.isCheck) {
            ContextCompat.getDrawable(context, R.drawable.bg_lang_selected)
        } else {
            ContextCompat.getDrawable(context, R.drawable.bg_lang_unselected)
        }

        binding.imvCheck.setImageResource(
            if (item.isCheck) R.drawable.ic_lang_checked
            else R.drawable.ic_lang_unchecked
        )
    }

    fun setSelectLang(lang: LanguageSelector) {
        val updatedPositions = mutableListOf<Int>()
        dataList.forEachIndexed { index, item ->
            val shouldCheck = lang.language.languageCode == item.language.languageCode
            if (item.isCheck != shouldCheck) {
                item.isCheck = shouldCheck
                updatedPositions.add(index)
            }
        }
        updatedPositions.forEach { position ->
            notifyItemChanged(position)
        }
    }
}

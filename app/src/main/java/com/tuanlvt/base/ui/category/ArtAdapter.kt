package com.tuanlvt.base.ui.category

import com.brally.mobile.base.adapter.BaseRecyclerViewAdapter
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.utils.loadImageUrl
import com.tuanlvt.base.databinding.ItemArtBinding

class ArtAdapter : BaseRecyclerViewAdapter<ArtItem, ItemArtBinding>() {

    override fun bindData(binding: ItemArtBinding, item: ArtItem, position: Int) {
        binding.imvArt.loadImageUrl(item.imagePath)
    }
}

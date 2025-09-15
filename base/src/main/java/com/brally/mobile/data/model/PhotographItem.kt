package com.brally.mobile.data.model

import androidx.annotation.DrawableRes
import com.brally.mobile.base.R

data class PhotographItem(
    var value: String = "",
    @DrawableRes val iconResId: Int = R.drawable.img_placeholder
)

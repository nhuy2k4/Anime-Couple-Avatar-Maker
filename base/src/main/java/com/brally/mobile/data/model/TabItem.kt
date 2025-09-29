package com.brally.mobile.data.model

data class TabItem(
    val id: String,        // Mã duy nhất của tab, ví dụ: "eyes", "hair", "mouth"
    val name: String,      // Tên hiển thị của tab
    val iconRes: Int       // Drawable resource id, ví dụ R.drawable.tab_eyes
)
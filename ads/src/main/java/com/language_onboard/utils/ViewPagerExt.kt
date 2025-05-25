package com.language_onboard.utils

import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.setPageChangeListener(
    onPageSelected: (Int) -> Unit
) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onPageSelected.invoke(position)
        }
    })
}

fun ViewPager2.nextPage() {
    val page = currentItem
    setCurrentItem(page + 1, true)
}
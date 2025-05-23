package com.language_onboard.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.data.model.OnboardingType
import com.language_onboard.ui.fragment.onboarding.CommonOnboardingImageFragment

class OnboardingPagerAdapter(
    fragment: Fragment,
    private val onBoardingItems: List<OnboardingItem>,
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = onBoardingItems.size

    override fun createFragment(position: Int): Fragment {
        val item = onBoardingItems[position]
        return when (item.type) {
            OnboardingType.IMAGE.type -> {
                CommonOnboardingImageFragment.newInstance(item)
            }

            else -> {
                CommonOnboardingImageFragment.newInstance(item)
            }
        }

    }
}
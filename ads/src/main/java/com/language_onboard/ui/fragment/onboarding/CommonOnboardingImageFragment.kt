package com.language_onboard.ui.fragment.onboarding

import android.os.Bundle
import android.view.View
import com.braly.ads.databinding.FragmentOnboardingImageCommonBinding
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.ui.BaseFragment
import com.language_onboard.utils.loadImageDrawable

class CommonOnboardingImageFragment : BaseFragment<FragmentOnboardingImageCommonBinding>() {
    override fun setSystemBarsColor() = Unit
    override fun initView() {
        super.initView()
        val item = arguments?.getParcelable<OnboardingItem>(ONBOARDING_ITEM)

        item?.let {
            binding.tvTitleOnboarding.text = it.title?.let { getString(it) } ?: ""
            binding.ivOnboarding.visibility = View.VISIBLE
            it.imageRes?.let { imageRes ->
                binding.ivOnboarding.loadImageDrawable(imageRes)
            }
        }
    }

    override fun getViewBinding(): FragmentOnboardingImageCommonBinding =
        FragmentOnboardingImageCommonBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
    }

    companion object {

        const val ONBOARDING_ITEM = "ONBOARDING_ITEM"
        fun newInstance(item: OnboardingItem) = CommonOnboardingImageFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ONBOARDING_ITEM, item)
            }
        }
    }
}
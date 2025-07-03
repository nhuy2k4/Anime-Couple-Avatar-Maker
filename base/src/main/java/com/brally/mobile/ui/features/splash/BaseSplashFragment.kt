package com.brally.mobile.ui.features.splash

import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.brally.mobile.base.R
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.service.ads.AdManager
import com.brally.mobile.service.event.SPLASH_SHOW
import com.brally.mobile.service.event.SPLASH_SHOW_0
import com.brally.mobile.service.event.subscribeEventNetwork
import com.brally.mobile.service.session.isFirst
import com.brally.mobile.service.session.isFirstSplash
import com.brally.mobile.service.session.setFirstSplash
import com.language_onboard.data.model.Language
import com.language_onboard.data.model.OnboardingConfig
import com.language_onboard.data.model.OnboardingItem
import com.language_onboard.data.model.OnboardingType
import com.language_onboard.utils.openOnboarding
import com.language_onboard.utils.tracking
import kotlinx.coroutines.launch

abstract class BaseSplashFragment<VB : ViewBinding, VM : BaseViewModel> :
    BaseFragment<VB, VM>() {

    override fun initView() {
        if (isFirstSplash()) {
            setFirstSplash(false)
            tracking(SPLASH_SHOW_0)
        }
        tracking(SPLASH_SHOW)
    }

    override fun initListener() {
    }


    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        subscribeEventNetwork { online ->
            if (online && isAdded) {
//                openHome()
                fetchAndInitAds()
            }
        }
    }

    private fun openOnboardingScreen() {
        val onboardingItems = listOf(
            OnboardingItem(
                type = OnboardingType.IMAGE.type,
                title = null,
                imageRes = R.drawable.img_onboard_1,
                nativeAdsLayoutRes = R.layout.layout_native_onboard
            ), OnboardingItem(
                type = OnboardingType.IMAGE.type,
                title = null,
                imageRes = R.drawable.img_onboard_2,
                nativeAdsLayoutRes = R.layout.layout_native_onboard
            ), OnboardingItem(
                type = OnboardingType.IMAGE.type,
                title = null,
                imageRes = R.drawable.img_onboard_3,
                nativeAdsLayoutRes = R.layout.layout_native_onboard,
            )
        )
        val languages = Language.entries

        val onboardingConfig = OnboardingConfig(
            languages = languages,
            onboardingItems = onboardingItems,
            languageNativeRes = R.layout.layout_native_onboard,
            nativeFullRes = R.layout.layout_native_ads_onboarding_full,
            isHideStatusBar = true
        )
        openOnboarding(onboardingConfig)
    }

    private fun fetchAndInitAds() {
        activity?.let {
            AdManager.fetchAndShowAds(activity = it,
                fragment = this,
                onAdDismiss = {
                    lifecycleScope.launch {
                        if (isFirst()) {
                            openOnboardingScreen()
                        } else {
                            openHome()
                        }
                    }
                })
        }
    }

    abstract fun bannerView(): ViewGroup?
    abstract fun openHome()
}

package com.language_onboard.utils

import android.app.Activity
import android.os.Handler
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.braly.ads.NativeAdView
import com.braly.ads.data.ConfigManager
import com.braly.ads.ads.BralyAdvertisement

object AdManager {
    // placement
    const val NATIVE_LANGUAGE = "native_language"
    const val BANNER_LANGUAGE = "banner_language"
    const val NATIVE_OB_FULL = "native_onboard_full"
    const val NATIVE_OB1 = "native_onboard1"
    const val NATIVE_OB2 = "native_onboard2"
    const val NATIVE_OB3 = "native_onboard3"

    fun commonIsAdsPlacementEnable(activity: Activity, adKey: String): Boolean {
        val configManager = ConfigManager.getInstance(activity)
        return configManager.adManagement?.adPlacements?.getOrElse(adKey) { null }?.enable ?: false
    }

    fun showNativeWithHandler(
        activity: Activity,
        fragment: Fragment,
        handler: Handler,
        view: NativeAdView,
        key: String,
        nativeLayoutRes: Int? = null,
    ) {
    }

    fun showNative(
        fragment: Fragment,
        view: NativeAdView,
        key: String,
        nativeLayoutRes: Int? = null,
    ) {
    }

    fun showBanner(activity: Activity, view: ViewGroup, key: String) {
        BralyAdvertisement.getInstance(activity).loadAndShowBanner(activity, view, key)
    }

    fun shouldShowOnboardingNativeFull(activity: Activity): Boolean {

        return false
    }
}





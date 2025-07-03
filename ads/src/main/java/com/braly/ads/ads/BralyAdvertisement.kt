package com.braly.ads.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.braly.ads.NativeAdView
import com.braly.ads.data.ConfigManager
import com.braly.ads.data.Configuration
import com.braly.ads.ads.interf.BralyResultConsentForm
import com.braly.ads.service.ads.BralyBannerAdListener

class BralyAdvertisement private constructor(context: Context) {

    fun initAdsAndLoadSplashHasBanner(
        application: Application,
        fragment: Fragment,
        bannerView: ViewGroup? = null,
        onAdsDismiss: Runnable
    ) {
        val activity = fragment.activity ?: return
        onAdsDismiss.run()
    }

    // Banner
    fun loadAndShowBanner(
        activity: Activity,
        viewGroup: ViewGroup,
        placementKey: String,
        callback: BralyBannerAdListener? = null
    ) {
    }

    private fun isShowDirectFull(activity: Activity, placementKey: String) = true

    fun fetchConfig(
        activity: Activity, unitConfig: String, placementConfig: String, itemConfig: String, callback: Runnable?
    ): BralyAdvertisement = apply {
        ConfigManager.Companion.getInstance(activity)
            .fetchConfig(activity, unitConfig, placementConfig, callback)
    }

    fun resetConfig(activity: Activity) = apply {}

    fun isShowingInterstitial(isShowing: Boolean) {
    }

    fun isNativeLoaded(placementKey: String): Boolean {
        return false
    }

    fun showNativeAndReload(
        fragment: Fragment, placementKey: String, view: NativeAdView
    ) = apply {

    }

    fun loadAndShowNative(
        fragment: Fragment,
        nativeAdView: NativeAdView,
        placementKey: String,
    ) = apply {}

    fun isRewardLoaded(placementKey: String) = true

    fun isRewardInterLoaded(placementKey: String) = true

    fun loadReward(activity: Activity, configKey: String, callback: Runnable? = null) = apply {}

    fun showRewardAndReload(activity: Activity, placementKey: String, listener: BralyRewardManagement.RewardListener?) =
        apply {}

    fun showInterstitialAndReload(activity: Activity, placementKey: String, runnable: Runnable?) =
        apply { runnable?.run() }

    fun checkToRegisterOpenAdsOnMain(activity: Activity) {}

    fun onIronSourceResume(activity: Activity) {}

    fun onIronSourcePause(activity: Activity) {}

    fun blockShowAdsSplash() {
    }

    fun resumeShowSplashAds(activity: Activity) {
    }

    fun unregisterOpenAds(activity: Activity) {
    }

    fun showConsentForm(
        activity: Activity,
        testDeviceIds: List<String>? = null,
        isForceShowConsentWhenRejectBefore: Boolean,
        resultConsentForm: BralyResultConsentForm
    ) {}

    fun showPrivacyOptionForm(
        activity: Activity, testDeviceId: String? = null, resultConsentForm: BralyResultConsentForm
    ) {}

    fun canShowCMPSetting(activity: Activity): Boolean = false

    companion object {
        private var sAdvertisement: BralyAdvertisement? = null

        @JvmStatic
        fun getInstance(context: Context): BralyAdvertisement {
            if (sAdvertisement == null) {
                sAdvertisement = BralyAdvertisement(context)
            }
            return sAdvertisement!!
        }

        @JvmStatic
        fun initialize(
            application: Application, configuration: Configuration, callback: Runnable? = null
        ) {
        }
    }

    fun loadNative(activity: Context, placementKey: String, callback: Runnable?) = apply {
        callback?.run()
    }

}
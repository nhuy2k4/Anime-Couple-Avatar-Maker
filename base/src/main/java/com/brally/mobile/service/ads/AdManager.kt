package com.brally.mobile.service.ads

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.brally.mobile.utils.callSafeFragment
import com.braly.ads.NativeAdView
import com.braly.ads.SmallDelayCallback
import com.braly.ads.ads.BralyAdvertisement
import com.braly.ads.ads.BralyRewardManagement
import com.braly.ads.ads.interf.BralyResultConsentForm
import com.braly.ads.ads.interf.BralyRewardItem
import com.braly.ads.data.ConfigManager
import com.language_onboard.utils.gone

object AdManager {
    private const val ADS_CONFIG_KEY = "ads_config_unit"
    private const val PLACEMENT_KEY = "ads_config_placement"
    private const val ITEM_KEY = "ads_config_item"

    const val OPEN = "open"
    const val SPLASH = "splash"
    const val NATIVE_VIEW_LIST = "native_view_list"

    const val FULL_GETSTARTED = "full_getstarted"
    const val FULL_HOME = "full_home"
    const val FULL_TEMPLATE = "full_template"
    const val FULL_DRAW = "full_draw"
    const val FULL_BACK = "full_back"
    const val FULL_DONE = "full_done"
    const val FULL_BACKUP = "full_backup"

    const val NATIVE_ONBOARD1 = "native_onboard1"
    const val NATIVE_ONBOARD2 = "native_onboard2"
    const val NATIVE_ONBOARD3 = "native_onboard3"
    const val NATIVE_ONBOARD_FULL = "native_onboard_full"
    const val NATIVE_LANGUAGE = "native_language"
    const val NATIVE_HOME = "native_home"
    const val NATIVE_TEMPLATE = "native_template"
    const val NATIVE_BG = "native_bg"
    const val NATIVE_DRAW = "native_draw"
    const val NATIVE_RESULT = "native_result"
    const val NATIVE_ARTWORK = "native_artwork"


    fun fetchAndShowAds(
        activity: Activity, fragment: Fragment, onAdDismiss: Runnable,
    ) {
        val bralyAdvertisement = BralyAdvertisement.Companion.getInstance(activity)
        bralyAdvertisement.fetchConfig(
            activity, ADS_CONFIG_KEY, PLACEMENT_KEY, ITEM_KEY
        ) {

            bralyAdvertisement.initAdsAndLoadSplashHasBanner(
                application = activity.application,
                fragment = fragment,
                bannerView = null,
                onAdsDismiss = {
                    onAdDismiss.run()
                })
        }
    }

    fun showBanner(activity: Activity, view: ViewGroup, key: String) {
        BralyAdvertisement.Companion.getInstance(activity).loadAndShowBanner(activity, view, key)
    }

    fun showNative(
        activity: Activity, view: NativeAdView, placementKey: String, fragment: Fragment
    ) {
        val advertisement = BralyAdvertisement.Companion.getInstance(activity)
        val enableNative = isAdsPlacementEnable(activity, placementKey)
        if (enableNative) {
            if (advertisement.isNativeLoaded(placementKey)) {
                advertisement.showNativeAndReload(fragment, placementKey, view)
            } else {
                advertisement.loadAndShowNative(fragment, view, placementKey)
            }
        } else view.visibility = View.GONE

    }

    fun showReward(
        activity: Activity, key: String, onGrantReward: Runnable, onLoadFail: Runnable
    ) {
        val management = BralyAdvertisement.Companion.getInstance(activity)
        val rewardListener = object : BralyRewardManagement.RewardListener() {
            override fun onRewardAdded(rewardItem: BralyRewardItem?) {
                onGrantReward.run()
            }

            override fun onRewardFail(error: String?) {
                onLoadFail.run()
            }

        }
        if (management.isRewardLoaded(key)) {

            management.showRewardAndReload(activity, key, rewardListener)
        } else {
            management.loadReward(activity, key) {
                management.showRewardAndReload(activity, key, rewardListener)
            }
        }
    }


    fun showFull(activity: Activity, placementKey: String, runnable: Runnable) {
        BralyAdvertisement.Companion.getInstance(activity)
            .showInterstitialAndReload(activity, placementKey, SmallDelayCallback(runnable))
    }


    fun Fragment.showFull(key: String, runnable: Runnable) {
        activity?.let {
            showFull(it, key) {
                runnable.run()
            }
        }
    }

    fun checkToRegisterOpenAdsOnMain(activity: Activity) {
        BralyAdvertisement.Companion.getInstance(activity).checkToRegisterOpenAdsOnMain(activity)
    }


    fun onIronSourceResume(activity: Activity) {
        BralyAdvertisement.Companion.getInstance(activity).onIronSourceResume(activity)
    }

    fun resumeShowAdsSplash(activity: Activity) {
        BralyAdvertisement.Companion.getInstance(activity).resumeShowSplashAds(activity)
    }

    fun blockShowAdsSplash(activity: Activity) {
        BralyAdvertisement.Companion.getInstance(activity).blockShowAdsSplash()
    }

    fun unregisterOpenAds(activity: Activity) {
        BralyAdvertisement.Companion.getInstance(activity).unregisterOpenAds(activity)
    }

    fun onIronSourcePause(activity: Activity) {
        BralyAdvertisement.Companion.getInstance(activity).onIronSourcePause(activity)
    }

    fun showConsentForm(
        activity: Activity,
        testDeviceId: List<String>? = null,
        isForceShowConsentWhenRejectBefore: Boolean,
        resultConsentForm: BralyResultConsentForm
    ) {
        BralyAdvertisement.Companion.getInstance(activity).showConsentForm(
            activity, testDeviceId, isForceShowConsentWhenRejectBefore, resultConsentForm
        )
    }

    fun showPrivacyOptionForm(
        activity: Activity, testDeviceId: String? = null, resultConsentForm: BralyResultConsentForm
    ) {
        BralyAdvertisement.Companion.getInstance(activity).showPrivacyOptionForm(
            activity, testDeviceId, resultConsentForm
        )
    }

    fun isAdsPlacementEnable(activity: Activity, adKey: String): Boolean {
        return ConfigManager.getInstance(activity).adManagement?.adPlacements?.getOrElse(adKey) { null }?.placementConfig?.enable
            ?: false
    }

    fun isCmpRequired(activity: Activity): Boolean {
        return BralyAdvertisement.Companion.getInstance(activity).canShowCMPSetting(activity)
    }
}

fun Fragment.registerOpenAd() {
    activity?.let {
        AdManager.checkToRegisterOpenAdsOnMain(it)
    }
}

fun Fragment.unregisterOpenAd() {
    activity?.let {
        AdManager.unregisterOpenAds(it)
    }
}

fun Fragment.showPrivacyOptionForm(resultConsentForm: BralyResultConsentForm) {
    activity?.let {
        AdManager.showPrivacyOptionForm(activity = it, resultConsentForm = resultConsentForm)
    }
}

fun Fragment.showNative(key: String, nativeAdView: NativeAdView) {
    activity?.let {
        AdManager.showNative(it, nativeAdView, key, this)
    }
}

fun Fragment.showBanner(viewGroup: ViewGroup, configKey: String) {
    activity?.let {
        AdManager.showBanner(it, viewGroup, configKey)
    }
}

fun Fragment.showFull(key: String, runnable: Runnable) {
    activity?.let {
        AdManager.showFull(it, key) {
            callSafeFragment {
                runnable.run()
            }
        }
    }
}

fun Fragment.showReward(
    adKey: String, onGrantReward: Runnable, onLoadFail: Runnable
) {
    activity?.let {
        AdManager.showReward(it, adKey, onGrantReward, onLoadFail)
    }
}

fun Fragment.showBannerOrNative(
    bannerKey: String,
    nativeKey: String,
    bannerView: ViewGroup,
    nativeView: NativeAdView,
    collapsibleIcon: View? = null
) {
    val enableBanner = AdManager.isAdsPlacementEnable(requireActivity(), bannerKey)
    val enableNative = AdManager.isAdsPlacementEnable(requireActivity(), nativeKey)
    if (enableNative) {
        showNative(nativeKey, nativeView)
        bannerView.gone()
    } else if (enableBanner) {
        showBanner(bannerView, bannerKey)
        nativeView.gone()
        collapsibleIcon?.gone()
    } else {
        nativeView.gone()
        bannerView.gone()
        collapsibleIcon?.gone()
    }
}


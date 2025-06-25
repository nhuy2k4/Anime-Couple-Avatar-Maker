package com.language_onboard.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.braly.ads.NativeAdView
import com.braly.ads.R
import com.braly.analytics.event.BralyTracking
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.language_onboard.data.model.OnboardingConfig
import com.language_onboard.ui.fragment.onboarding.CommonOnBoardingModuleFragmentArgs
import com.language_onboard.utils.AdManager.showBanner
import com.language_onboard.utils.AdManager.showNative
import java.util.Locale

fun Fragment.openOnboarding(
    onboardingConfig: OnboardingConfig
) {
    if (!isAdded || isDetached || activity == null) return
    val bundle = CommonOnBoardingModuleFragmentArgs(onboardingConfig).toBundle()
    findNavController().navigate(R.id.nav_onboarding, bundle)
}

fun ImageView.loadImage(@DrawableRes icon: Int) {
    val requestBuilder: RequestBuilder<Drawable> = Glide.with(this)
        .asDrawable().sizeMultiplier(0.1f)

    Glide.with(this)
        .load(icon)
        .thumbnail(requestBuilder)
        .into(this)
}

fun ImageView.loadImageDrawable(@DrawableRes icon: Int) {
    val requestBuilder: RequestBuilder<Drawable> = Glide.with(this)
        .asDrawable().sizeMultiplier(0.1f)

    Glide.with(this)
        .load(icon)
        .thumbnail(requestBuilder)
        .into(this)
}

fun Context.setLocale(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}

fun Fragment.onBackPressed(runnable: Runnable) {
    activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                runnable.run()
            }
        })
}

fun Fragment.tracking(eventName: String, params: HashMap<String, String>? = null) {
    context?.tracking(eventName, params)
}

fun Context.tracking(eventName: String, params: HashMap<String, String>? = null) {
    BralyTracking.logEvent(this, eventName, params)
}

fun Fragment.showBannerOrNative(
    activity: Activity,
    bannerKey: String,
    nativeKey: String,
    bannerView: ViewGroup,
    nativeView: NativeAdView,
    nativeLayoutRes: Int? = null,
) {
    val enableBanner = AdManager.commonIsAdsPlacementEnable(requireActivity(), bannerKey)
    val enableNative = AdManager.commonIsAdsPlacementEnable(requireActivity(), nativeKey)
    if (enableNative) {
        showNative(this, nativeView, nativeKey, nativeLayoutRes)
        bannerView.gone()
    } else if (enableBanner) {
        showBanner(activity, bannerView, bannerKey)
        nativeView.gone()
    } else {
        nativeView.gone()
        bannerView.gone()
    }
}

fun Fragment.hideNavBar() {
    try {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).also { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
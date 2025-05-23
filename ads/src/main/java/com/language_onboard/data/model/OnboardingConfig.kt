package com.language_onboard.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.braly.ads.R
import kotlinx.parcelize.Parcelize

enum class OnboardingType(
    val type: String,
) {
    IMAGE("image"), ADVERTISEMENT("advertisement")
}

@Keep
@Parcelize
data class OnboardingItem(
    val type: String = OnboardingType.IMAGE.type,
    val title: Int? = null,
    val imageRes: Int? = null,
    val videoRes: Int? = null,
    val nativeAdsLayoutRes: Int = R.layout.admob_native_ad_view,
) : Parcelable

@Parcelize
data class OnboardingConfig(
    val languages: List<Language> = Language.entries,
    val languageNativeRes: Int = R.layout.admob_native_ad_view,
    val nativeFullRes: Int = R.layout.view_full_screen_native,
    val onboardingItems: List<OnboardingItem>,
    val nativeKeyLanguage: String? = null,
    val nativeKeyOnboard: String? = null,
    val textFont: Int = R.font.poppins_bold,
    val isHideStatusBar: Boolean = false
) : Parcelable



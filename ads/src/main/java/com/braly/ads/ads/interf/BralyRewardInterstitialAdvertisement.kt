package com.braly.ads.ads.interf

import android.app.Activity

interface BralyRewardInterstitialAdvertisement {
    fun load(activity: Activity, callback: BralyRewardInterstitialAdCallback?): BralyRewardInterstitialAdvertisement?
    val isLoaded: Boolean
    fun show(
        activity: Activity,
        callback: BralyRewardContentCallback?,
        rewardItemCallback: BralyRewardItemCallback?
    )
}
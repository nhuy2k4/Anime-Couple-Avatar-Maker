package com.braly.ads.interf

import BralyRewardItemCallback
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
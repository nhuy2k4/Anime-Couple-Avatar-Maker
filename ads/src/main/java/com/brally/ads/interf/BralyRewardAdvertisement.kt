package com.brally.ads.interf

import BralyRewardItemCallback
import android.app.Activity

interface BralyRewardAdvertisement {
    fun load(activity: Activity, callback: BralyRewardAdCallback?): BralyRewardAdvertisement?
    val isLoaded: Boolean
    fun show(
        activity: Activity,
        callback: BralyRewardContentCallback?,
        rewardItemCallback: BralyRewardItemCallback?
    )
}
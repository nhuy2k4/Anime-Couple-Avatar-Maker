package com.brally.ads.ads.interf

import BralyRewardItemCallback
import android.app.Activity
import com.braly.ads.interf.BralyRewardContentCallback

interface BralyRewardAdvertisement {
    fun load(activity: Activity, callback: BralyRewardAdCallback?): BralyRewardAdvertisement?
    val isLoaded: Boolean
    fun show(
        activity: Activity,
        callback: BralyRewardContentCallback?,
        rewardItemCallback: BralyRewardItemCallback?
    )
}
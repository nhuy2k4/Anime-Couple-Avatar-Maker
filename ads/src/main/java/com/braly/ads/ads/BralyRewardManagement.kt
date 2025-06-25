package com.braly.ads.ads

import android.content.Context
import com.braly.ads.ads.interf.BralyRewardContentCallback
import com.braly.ads.ads.interf.BralyRewardItem
import com.braly.ads.ads.interf.BralyRewardItemCallback

class BralyRewardManagement(context: Context) {
    private val loadingPools: MutableSet<String> = mutableSetOf()

    /************ PUBLIC REGION  */
    val isShowingAds = false



    abstract class RewardListener : BralyRewardContentCallback, BralyRewardItemCallback {
        private var item: BralyRewardItem? = null
        abstract fun onRewardAdded(rewardItem: BralyRewardItem?)
        abstract fun onRewardFail(error: String?)
        override fun onAdFailedToShowFullScreenContent(error: String?) {
            onRewardFail(error)
        }

        override fun onAdShowedFullScreenContent() {}
        override fun onAdDismissedFullScreenContent() {
            if (item != null) {
                onRewardAdded(item)
            } else {
                onRewardFail("")
            }
        }

        override fun onUserEarnedReward(rewardItem: BralyRewardItem?) {
            item = rewardItem
        }
    }
}


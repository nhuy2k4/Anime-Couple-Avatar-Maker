package com.brally.ads.service.ads

import BralyRewardItemCallback
import android.content.Context
import com.brally.ads.interf.BralyRewardContentCallback

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

class BralyRewardItem(private val type: String, private val amount: Int) {
    companion object {
        @JvmField
        var DEFAULT = BralyRewardItem("DEFAULT", 0)
    }
}

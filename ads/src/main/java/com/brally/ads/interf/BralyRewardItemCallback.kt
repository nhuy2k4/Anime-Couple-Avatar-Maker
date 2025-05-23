import com.brally.ads.service.ads.BralyRewardItem

interface BralyRewardItemCallback {
    fun onUserEarnedReward(rewardItem: BralyRewardItem?)
}
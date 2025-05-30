import com.braly.ads.service.ads.BralyRewardItem

interface BralyRewardItemCallback {
    fun onUserEarnedReward(rewardItem: BralyRewardItem?)
}
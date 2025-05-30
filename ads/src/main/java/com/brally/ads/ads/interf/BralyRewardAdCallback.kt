package com.brally.ads.ads.interf

interface BralyRewardAdCallback {
    fun onAdLoaded(interstitialAds: BralyRewardAdvertisement?)
    fun onAdFailedToLoad(error: String?)
}
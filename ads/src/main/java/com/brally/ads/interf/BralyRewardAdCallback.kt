package com.brally.ads.interf

interface BralyRewardAdCallback {
    fun onAdLoaded(interstitialAds: BralyRewardAdvertisement?)
    fun onAdFailedToLoad(error: String?)
}
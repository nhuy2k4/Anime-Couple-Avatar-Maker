package com.braly.ads.ads.interf

interface BralyRewardAdCallback {
    fun onAdLoaded(interstitialAds: BralyRewardAdvertisement?)
    fun onAdFailedToLoad(error: String?)
}
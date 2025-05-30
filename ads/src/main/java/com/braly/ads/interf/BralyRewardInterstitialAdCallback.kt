package com.braly.ads.interf

interface BralyRewardInterstitialAdCallback {
    fun onAdLoaded(interstitialAds: BralyRewardInterstitialAdvertisement?)
    fun onAdFailedToLoad(error: String?)
}
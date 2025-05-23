package com.brally.ads.interf

interface BralyRewardContentCallback {
    fun onAdFailedToShowFullScreenContent(error: String?)
    fun onAdShowedFullScreenContent()
    fun onAdDismissedFullScreenContent()
}
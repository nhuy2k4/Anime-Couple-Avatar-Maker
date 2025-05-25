package com.brally.ads.service.ads

import android.view.View

interface BralyBannerAdListener {
    fun onBannerLoaded(view: View?)

    fun onBannerFailed(errorCode: String?)
}
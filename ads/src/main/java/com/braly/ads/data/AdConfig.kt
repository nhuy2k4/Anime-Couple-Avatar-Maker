package com.braly.ads.data

class AdConfig {
    var enable: Boolean = true
    var adInterval: Int? = null
        get() = if (field == null) 30 else field
    var bannerInterval: Int = 0
    var preloadBanner: Boolean = false
    var attemptLoadLimit: Int = -1
    var delayWaterfallCall: Int = 0
}
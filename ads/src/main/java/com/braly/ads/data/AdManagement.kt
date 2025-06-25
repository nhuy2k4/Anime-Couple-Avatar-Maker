package com.braly.ads.data

class AdManagement {
    var adConfig: AdConfig? = null
    var adUnitResponse: Map<String, List<AdUnitItem>>? = null
    var adPlacements: Map<String, AdPlacement>? = null

    var isEnable: Boolean
        get() = adConfig?.enable ?: false
        set(isEnable) {
            adConfig?.enable = isEnable
        }
}
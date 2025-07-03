package com.braly.ads.data

data class AdPlacement(
    val unit: String,
    var enable: Boolean = true,
    var placementConfig: AdPlacement? = null
    ) {

}
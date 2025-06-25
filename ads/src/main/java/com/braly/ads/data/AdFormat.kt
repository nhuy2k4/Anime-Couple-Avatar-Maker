package com.braly.ads.data

enum class AdFormat(val formatName: String) {
    BANNER("banner"),
    OPEN("open"),
    NATIVE("native"),
    FULLSCREEN_NATIVE("fsn"),
    INTERSTITIAL("full"),
    REWARD("reward"),
    REWARD_INTERSTITIAL("inter_reward"),
    MREC("mrec");

    companion object {
        fun fromFormatName(formatName: String): AdFormat? {
            return values().find { it.formatName == formatName }
        }
    }
}
package com.braly.ads.ads.interf

class BralyRewardItem(private val type: String, private val amount: Int) {
    companion object {
        @JvmField
        var DEFAULT = BralyRewardItem("DEFAULT", 0)
    }
}

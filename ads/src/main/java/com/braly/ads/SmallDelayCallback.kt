package com.braly.ads

import android.os.Handler

class SmallDelayCallback constructor(
    private val callback: Runnable
) : Runnable {
    override fun run() {
        Handler().postDelayed(callback, 100)
    }
}
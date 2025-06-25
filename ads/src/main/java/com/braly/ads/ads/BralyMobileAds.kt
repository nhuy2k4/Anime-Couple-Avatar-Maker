package com.braly.ads.ads

import android.app.Activity
import android.app.Application
import com.braly.analytics.BralyAnalytics
import com.braly.ads.data.Configuration

object BralyMobileAds {
    private var _configuration: Configuration? = null
    val configuration: Configuration get() = _configuration!!

    @Deprecated("This function is deprecated")
    fun setFirebaseConfigEnable(isEnable: Boolean) {

    }

    fun initConfig(application: Application, configuration: Configuration) {
        this._configuration = configuration
        configuration.appmetricaNetwork?.let {
            BralyAnalytics.initAppMetrica(application, it)
        }
    }

    fun initializeAds(application: Application, callback: Runnable?) {
        configuration.appmetricaNetwork?.let {
            BralyAnalytics.initAppMetrica(application, it)
        }
        BralyAdvertisement.initialize(application, configuration, callback)
    }

    fun showMaxMediationDebugger(activity: Activity) {
    }

    fun showBralyConfigDebugger(activity: Activity) {
    }
}
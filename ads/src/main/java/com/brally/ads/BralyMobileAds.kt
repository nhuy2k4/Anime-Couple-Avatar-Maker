package com.brally.ads

import android.app.Activity
import android.app.Application
import com.brally.ads.analytics.BralyAnalytics
import com.brally.ads.data.Configuration
import com.brally.ads.service.ads.BralyAdvertisement

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
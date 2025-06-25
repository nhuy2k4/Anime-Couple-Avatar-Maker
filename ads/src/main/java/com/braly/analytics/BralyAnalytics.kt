package com.braly.analytics

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

object BralyAnalytics {

    fun initAppMetrica(application: Application, appMetricaApiKey: String) {
        val config = AppMetricaConfig.newConfigBuilder(appMetricaApiKey).build()
        AppMetrica.activate(application.applicationContext, config)
    }
}

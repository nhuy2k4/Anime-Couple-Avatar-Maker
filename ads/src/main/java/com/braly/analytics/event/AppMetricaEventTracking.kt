package com.braly.analytics.event

import io.appmetrica.analytics.AppMetrica
import java.util.HashMap


class AppMetricaEventTracking : BralyEventTracking {


    override fun logEvent(
        eventName: String,
        params: HashMap<String, String>?,
        useMetricaTracking: Boolean,
    ) {
        try {
            AppMetrica.reportEvent(eventName, params?.toMap())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun logScreenView(screenName: String) {
        try {
            AppMetrica.reportEvent(screenName)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
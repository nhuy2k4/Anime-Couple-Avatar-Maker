package com.braly.analytics.event

import android.content.Context

class BralyEventTrackingImpl(
    private val context: Context
) : BralyEventTracking {

    private val firebaseTracking by lazy { FirebaseEventTracking(context) }
    private val appMetricaEventTracking by lazy { AppMetricaEventTracking() }
//    private val facebookEventTracking by lazy { FacebookEventTracking(context) }

    override fun logEvent(
        eventName: String,
        params: HashMap<String, String>?,
        useMetricaTracking: Boolean,
    ) {
        firebaseTracking.logEvent(eventName, params)
//        if (BralyMobileAds.configuration.isUseAppmetrica() && useMetricaTracking) {
//            appMetricaEventTracking.logEvent(eventName, params)
//        }
//        facebookEventTracking.logEvent(eventName, params)
    }

    override fun logScreenView(screenName: String) {
        firebaseTracking.logScreenView(screenName)
    }
}
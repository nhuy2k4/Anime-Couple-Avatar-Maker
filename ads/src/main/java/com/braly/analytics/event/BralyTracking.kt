package com.braly.analytics.event

import android.content.Context
import android.util.Log

object BralyTracking {
    fun logEvent(context: Context, eventName: String, params: HashMap<String, String>? = null, useMetricaEventTracking: Boolean = false) {
        val paramsStr = (" / params = " + params?.toString()).takeIf { params != null }.orEmpty()
        Log.e("TAG::", eventName + paramsStr)
        BralyEventTrackingImpl(context).logEvent(eventName, params, useMetricaEventTracking)
    }

    fun logScreenView(context: Context, screenName: String) {
        BralyEventTrackingImpl(context).logScreenView(screenName)
    }
}
package com.braly.analytics.event

import java.util.*

interface BralyEventTracking {
    fun logEvent(eventName: String, params: HashMap<String, String>? = null, useMetricaTracking: Boolean = false)

    fun logScreenView(screenName: String)
}

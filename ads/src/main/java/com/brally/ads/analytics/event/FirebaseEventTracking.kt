package com.brally.ads.analytics.event

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.braly.ads.analytics.event.BralyEventTracking
import com.google.firebase.analytics.FirebaseAnalytics

@SuppressLint("MissingPermission")
class FirebaseEventTracking constructor(
    private val context: Context
) : BralyEventTracking {

    private val firebaseEvent by lazy { FirebaseAnalytics.getInstance(context) }

    override fun logEvent(
        eventName: String,
        params: HashMap<String, String>?,
        useMetricaTracking: Boolean,
    ) {
        val bundle = Bundle()
        params?.entries?.forEach {
            bundle.putString(it.key, it.value)
        }
        firebaseEvent.logEvent(eventName, bundle)
    }

    override fun logScreenView(screenName: String) {
        firebaseEvent.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(
                FirebaseAnalytics.Param.SCREEN_NAME to screenName,
                FirebaseAnalytics.Param.SCREEN_CLASS to screenName
            )
        )
    }
}
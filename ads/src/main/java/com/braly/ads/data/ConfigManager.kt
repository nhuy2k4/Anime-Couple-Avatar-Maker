package com.braly.ads.data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.braly.analytics.config.BralyRemoteConfigImpl

class ConfigManager private constructor(
    private val context: Context
) {
    private val sharePreferences: SharedPreferences by lazy {
        context.getSharedPreferences("AppAdsConfig", Context.MODE_PRIVATE)
    }
    private var _adManagement: AdManagement? = null
        set(value) {
            field = value
        }

    var adManagement: AdManagement?
        set(value) {
            _adManagement = value
        }
        get() {

            return _adManagement
        }

    fun fetchConfig(
        activity: Activity,
        adsConfig: String?,
        placementConfig: String?,
        runnable: Runnable?
    ) {
        val remoteConfig = BralyRemoteConfigImpl()
        remoteConfig.fetchConfig {
            if (runnable != null && !activity.isFinishing) activity.runOnUiThread { runnable.run() }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var sInstance: ConfigManager? = null

        @JvmStatic
        fun getInstance(context: Context): ConfigManager {
            if (sInstance == null) {
                sInstance = ConfigManager(context)
            }
            return sInstance!!
        }
    }

}
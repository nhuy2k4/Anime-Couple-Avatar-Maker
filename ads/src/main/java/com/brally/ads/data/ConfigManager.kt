package com.brally.ads.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

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
            if (_adManagement == null) {
            }
            return _adManagement
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
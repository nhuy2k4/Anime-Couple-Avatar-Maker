package com.language_onboard.data.local

import android.content.Context
import android.content.res.Configuration
import java.util.Locale


class CommonAppSharePref(private val context: Context) {
    companion object {
        private const val PREF_SHOW_LANGUAGE = "pref_show_language"
        private const val PREF_LANGUAGE_CODE = "pref_language_code"
    }

    private val sharePref by lazy {
        context.getSharedPreferences("TrackingSharePref", Context.MODE_PRIVATE)
    }

    var isEnableLanguage: Boolean
        get() = sharePref.getBoolean(PREF_SHOW_LANGUAGE, false)
        set(value) {
            sharePref.edit().putBoolean(PREF_SHOW_LANGUAGE, value).apply()
        }

    var languageCode: String?
        get() = sharePref.getString(PREF_LANGUAGE_CODE, null)
        set(value) {
            sharePref.edit().putString(PREF_LANGUAGE_CODE, value).apply()
        }

    fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
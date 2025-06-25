package com.braly.analytics.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

internal class BralyFirebaseRemoteConfig : BralyRemoteConfig {
    private var onConfigActivated: Runnable? = null
    private val remoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    override fun getString(config: String): String? {
        return remoteConfig.getString(config)
    }

    override fun getLong(config: String): Long? {
        return remoteConfig.getLong(config)
    }

    override fun getBoolean(config: String): Boolean? {
        return remoteConfig.getBoolean(config)
    }

    override fun getDouble(config: String): Double? {
        return remoteConfig.getDouble(config)
    }

    override fun fetchConfig(runnable: Runnable?) {
        onConfigActivated = runnable
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                onConfigActivated?.run()
                onConfigActivated = null
            }
            .addOnFailureListener {
                onConfigActivated?.run()
                onConfigActivated = null
            }
            .addOnCompleteListener {
                onConfigActivated?.run()
                onConfigActivated = null
            }
    }

}
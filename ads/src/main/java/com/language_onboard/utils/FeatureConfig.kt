package com.language_onboard.utils

import com.braly.analytics.config.BralyRemoteConfigImpl
import com.braly.ads.BuildConfig
import com.language_onboard.data.model.CommonEnableConfig
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object FeatureConfig {
    private const val CONFIG_ENABLE_FEATURES = "config_enable_features"
    private const val CONFIG_TEXT_ONBOARDING = "config_text_onboarding"

    fun getConfigEnableFeatures(): CommonEnableConfig {
        if (BuildConfig.DEBUG) {
            return CommonEnableConfig(true, true)
        }
        val jsonString = BralyRemoteConfigImpl().getString(CONFIG_ENABLE_FEATURES).orEmpty()
        if (jsonString.isEmpty()) return CommonEnableConfig()
        val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter: JsonAdapter<CommonEnableConfig> =
            moshi.adapter(CommonEnableConfig::class.java)
        return jsonAdapter.fromJson(jsonString) ?: CommonEnableConfig()

    }

}
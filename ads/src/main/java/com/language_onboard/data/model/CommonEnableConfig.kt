package com.language_onboard.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CommonEnableConfig(
    @SerializedName("onboarding")
    val onboarding: Boolean? = true,
    @SerializedName("language")
    val language: Boolean? = true
)
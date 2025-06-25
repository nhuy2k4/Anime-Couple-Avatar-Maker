package com.braly.analytics.data

import org.json.JSONObject

data class ForceUpdateInfo(
    val title: String? = null,
    val des: String? = null,
    val packageName: String? = null,
    val ctaAction: String? = null,
) {
    companion object {
        fun fromJson(jsonObject: JSONObject?): ForceUpdateInfo? {
            if (jsonObject == null) return null
            try {
                return ForceUpdateInfo(
                    jsonObject.optString("title"),
                    jsonObject.optString("des"),
                    jsonObject.optString("package_name"),
                    jsonObject.optString("cta_action"),
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return null
        }
    }
}
package com.braly.analytics.data

import org.json.JSONObject

data class UpdateAppInfo(
    val lastVersionCode: Int? = null,
    val newFeature: String? = null,
    val isForceUpdate: Boolean = false
) {

    companion object {
        fun fromJson(versionInfo: JSONObject): UpdateAppInfo? {
            try {
                return UpdateAppInfo(
                    versionInfo.optInt("last_version", 0),
                    versionInfo.optString("new_feature"),
                    versionInfo.optBoolean("force_update", false)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun toJson(appVersionInfo: UpdateAppInfo): JSONObject? {
            try {
                val jsonObject = JSONObject()
                if (appVersionInfo.lastVersionCode != null) {
                    jsonObject.put("last_version", appVersionInfo.lastVersionCode)
                }
                if (appVersionInfo.newFeature != null) {
                    jsonObject.put("new_feature", appVersionInfo.newFeature)
                }
                jsonObject.put("force_update", appVersionInfo.isForceUpdate)
                return jsonObject
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}
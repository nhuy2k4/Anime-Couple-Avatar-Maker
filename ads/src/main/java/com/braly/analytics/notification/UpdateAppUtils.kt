package com.braly.analytics.notification

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import com.braly.analytics.Utilities
import com.braly.analytics.config.BralyRemoteConfigImpl
import com.braly.analytics.data.ForceUpdateInfo
import com.braly.analytics.data.UpdateAppInfo
import org.json.JSONObject

object UpdateAppUtils {

    fun checkUpdateApp(activity: Activity) {
        val updateStr = BralyRemoteConfigImpl().getString("version_info")
        val forceUpdateStr = BralyRemoteConfigImpl().getString("force_update")
        if (forceUpdateStr.isNullOrEmpty() && updateStr.isNullOrEmpty()) return
        val forceUpdateInfo = if (forceUpdateStr.isNullOrEmpty()) null else
            ForceUpdateInfo.fromJson(JSONObject(forceUpdateStr))

        if (forceUpdateInfo != null) {
            showForceUpdateApp(activity, forceUpdateInfo)
            return
        }

        val updateInfo =
            if (updateStr.isNullOrEmpty()) null else UpdateAppInfo.fromJson(JSONObject(updateStr))
        if (updateInfo != null) {
            if (shouldShowUpdate(activity, updateInfo)) {
                if (!updateInfo.isForceUpdate) saveShowUpdate(activity, updateInfo)
                showUpdateApp(activity, updateInfo)
            }
        }
    }

    private fun showUpdateApp(activity: Activity, updateInfo: UpdateAppInfo) {
        val alertDialogBuilder = AlertDialog.Builder(activity)
            .setTitle("New version available")
            .setMessage(
                updateInfo.newFeature ?: "New feature available, Open play store and update now."
            )
            .setCancelable(!updateInfo.isForceUpdate)
            .setPositiveButton("Update") { _, _ ->
                Utilities.gotoMarket(activity, activity.packageName)
            }

        if (!updateInfo.isForceUpdate) {
            alertDialogBuilder.setNegativeButton("Later") { _, _ -> }
        }
        alertDialogBuilder.show()
    }

    private fun showForceUpdateApp(activity: Activity, forceUpdateInfo: ForceUpdateInfo) {
        AlertDialog.Builder(activity)
            .setTitle(forceUpdateInfo.title)
            .setMessage(forceUpdateInfo.des)
            .setCancelable(false)
            .setPositiveButton(forceUpdateInfo.ctaAction) { _, _ ->
                Utilities.gotoMarket(activity, forceUpdateInfo.packageName)
            }
            .show()
    }

    private fun saveShowUpdate(activity: Activity, updateInfo: UpdateAppInfo) {
        activity.getSharedPreferences("update_info", Context.MODE_PRIVATE).edit()
            .putBoolean("show_update_app_${updateInfo.lastVersionCode}", false)
            .apply()
    }

    private fun shouldShowUpdate(activity: Activity, updateInfo: UpdateAppInfo): Boolean {
        if (getAppVersion(activity) >= updateInfo.lastVersionCode ?: 0) return false
        return activity.getSharedPreferences("update_info", Context.MODE_PRIVATE)
            .getBoolean("show_update_app_${updateInfo.lastVersionCode}", true)
    }

    private fun getAppVersion(activity: Activity): Int {
        return try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            0
        }
    }
}
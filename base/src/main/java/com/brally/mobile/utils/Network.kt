package com.brally.mobile.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.brally.mobile.base.application.getBaseApplication

fun isNetworkAvailable(): Boolean {
    val context = getBaseApplication()
    try {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val capabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities?.let {
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return false
}


fun FragmentActivity.openSettingNetWork() {
    try {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


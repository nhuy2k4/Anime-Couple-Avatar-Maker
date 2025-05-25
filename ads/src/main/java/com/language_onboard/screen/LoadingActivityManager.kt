package com.language_onboard.screen

import android.app.Activity

object LoadingActivityManager {
    private var isShowing: Boolean = false

    private val REQUEST_CODE_LOADING = 1001

    fun show(activity: Activity) {
        if (!isShowing) {
            isShowing = true
        }
    }

    fun dismiss(activity: Activity) {
        if (isShowing) {
            isShowing = false
            activity.finishActivity(REQUEST_CODE_LOADING)
        }
    }
}
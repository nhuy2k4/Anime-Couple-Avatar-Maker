package com.bg.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.CharacterIterator
import java.text.StringCharacterIterator

object Common {
    fun openAppInStore(context: Context, packageName: String?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun sendFeedback(
        context: Context, appName: String?, emailAddress: String, message: String?
    ) {

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")

        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        val subject = if (appName.isNullOrEmpty()) "Feedback"
        else "Feedback for $appName"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)

        if (message != null) {
            intent.putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            context.startActivity(intent)
        } catch (ex: Exception) {
            Toast.makeText(
                context, "Oops, Something went wrong", Toast.LENGTH_SHORT
            ).show()
            ex.printStackTrace()
        }
    }

    fun sendEmail(
        context: Context, emailAddress: String, message: String? = null
    ) {
        sendFeedback(context, null, emailAddress, message)
    }

    fun openMail(context: Context, emailAddress: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
        }
        context.startActivity(intent)
    }

    fun shareApp(context: Context, appName: String) {
        try {
            val appPackageName =
                context.packageName // getPackageName() from Context or Activity object
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
            var shareMessage = "I would like invite you to download this app\n\n"
            shareMessage = """
               ${shareMessage}https://play.google.com/store/apps/details?id=$appPackageName
               """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: java.lang.Exception) {
            //e.toString();
        }
    }

    fun openWebView(context: Context, webUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun humanReadableByteCountBin(bytes: Long): String {
        val absB = if (bytes == Long.MIN_VALUE) Long.MAX_VALUE else Math.abs(bytes)
        if (absB < 1024) {
            return "$bytes B"
        }
        var value = absB
        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }
        value *= java.lang.Long.signum(bytes).toLong()
        return String.format("%.1f %ciB", value / 1024.0, ci.current())
    }
}

fun Context.getLayoutRes(layoutName: String?): Int {
    if (layoutName.isNullOrEmpty()) return -1
    return try {
        resources.getIdentifier(layoutName, "layout", packageName)
    } catch (ex: Exception) {
        -1
    }
}
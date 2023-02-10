package com.picassos.betamax.android.core.utilities

import android.app.Activity
import android.app.UiModeManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.UI_MODE_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.picassos.betamax.android.R
import com.picassos.betamax.android.data.source.local.shared_preferences.SharedPreferences
import com.picassos.betamax.android.presentation.app.restrict.vpn_restriction.VpnRestrictedActivity
import java.io.Serializable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION", "UNCHECKED_CAST", "SimpleDateFormat")
object Helper {
    fun darkMode(context: Context) {
        val sharedPreferences = SharedPreferences(context)
        when (sharedPreferences.loadDarkMode()) {
            1 -> context.setTheme(R.style.AppTheme)
            2 -> context.setTheme(R.style.DarkTheme)
            3 -> {
                when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> context.setTheme(R.style.DarkTheme)
                    Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> context.setTheme(
                        R.style.AppTheme
                    )
                }
            }
        }
    }

    fun restrictVpn(activity: Activity) {
        if (Connectivity.instance.isVpnActive()) {
            activity.startActivity(Intent(activity, VpnRestrictedActivity::class.java))
            activity.finishAffinity()
        }
    }

    fun copyright(context: Context): String {
        return "© ${getCurrentYearInt()} " + capitalizeFirstChar(context.getString(R.string.app_name)) + ". " + context.getString(R.string.all_rights_reserved)
    }

    fun capitalizeFirstChar(value: String): String {
        return if (value != "") {
            value.substring(0, 1).uppercase(Locale.getDefault()) + value.substring(1)
        } else value
    }

    fun characterIcon(value: String): String {
        return if (value != "") {
            value.substring(0, 1).uppercase(Locale.getDefault())
        } else value
    }

    fun convertMinutesToHoursAndMinutes(minutes: Int): String {
        val hours = TimeUnit.MINUTES.toHours(java.lang.Long.valueOf(minutes.toLong()))
        val remainMinutes = minutes - TimeUnit.HOURS.toMinutes(hours)
        return String.format("%02dh %02dm", hours, remainMinutes)
    }

    fun verifyLicense(developedBy: String): Boolean {
        return when (developedBy.lowercase()) {
            "picassos" -> true
            else -> false
        }
    }

    fun getFormattedDateString(dateTime: String, format: String): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        var date: Date? = null
        try {
            date = simpleDateFormat.parse(dateTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date == null) {
            return ""
        }
        val convertDateFormat = SimpleDateFormat(format)
        return convertDateFormat.format(date)
    }

    fun getCurrentYearInt(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    fun handleWebIntent(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun handleEmailIntent(context: Context, email: String, subject: String?, text: String?) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    fun handleWhatsappIntent(context: Context, number: String) {
        val url = "https://api.whatsapp.com/send?phone=$number"
        try {
            val packageManager: PackageManager = context.packageManager
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    fun parseUrl(url: String): String {
        var parsedUrl: String
        with(url) {
            when {
                contains("www.dropbox.com") -> {
                    parsedUrl = url.replace("www.dropbox.com", "dl.dropbox.com")
                    if (url.contains("?dl=0")) {
                        parsedUrl.replace("?dl=0", "?dl=1")
                    } else {
                        parsedUrl += "?dl=1"
                    }
                    return parsedUrl
                }
                else -> return url
            }
        }
    }

    fun isTelevision(context: Context): Boolean {
        val uiModeManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
    
    fun Context.isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.intent.getSerializableExtra(name, clazz)!!
        } else {
            activity.intent.getSerializableExtra(name) as T
        }
    }

    fun <T : Serializable?> getBundleSerializable(arguments: Bundle, name: String, clazz: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments.getSerializable(name, clazz)!!
        } else {
            arguments.getSerializable(name) as T
        }
    }
}
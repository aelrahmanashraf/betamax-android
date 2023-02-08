package com.picassos.betamax.android.data.source.local.shared_preferences

import android.content.Context
import android.content.SharedPreferences
import com.picassos.betamax.android.R

class SharedPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    fun setDarkMode(state: Int?) {
        val editor = sharedPreferences.edit()
        editor.putInt("DarkMode", state!!)
        editor.apply()
    }

    fun loadDarkMode(): Int {
        return sharedPreferences.getInt("DarkMode", 2)
    }

    fun setSubscription(state: Boolean?) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("SubscriptionPage", state!!)
        editor.apply()
    }

    fun loadSubscription(): Boolean {
        return sharedPreferences.getBoolean("SubscriptionPage", false)
    }
}
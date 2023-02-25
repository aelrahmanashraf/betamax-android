package com.picassos.betamax.android.core.utilities

import android.os.Build
import java.util.*

@Suppress("DEPRECATION")
object Security {
    fun getDeviceImei(): String {
        return "35" +
            Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 +
            Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 +
            Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 +
            Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 +
            Build.USER.length % 10
    }

    fun generateRandomUUID(): String {
        return UUID.randomUUID().toString()
    }

    fun encodeStringToBase64(input: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(input.toByteArray())
        } else {
            return input
        }
    }
}
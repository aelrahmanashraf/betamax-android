package com.picassos.betamax.android.core.utilities

import android.os.Build
import java.util.*

object Security {
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
package com.picassos.betamax.android.domain.model

import com.picassos.betamax.android.core.utilities.Response.CONFIGURATION_NOT_SET
import java.io.Serializable

data class Configuration(
    val email: String = CONFIGURATION_NOT_SET,
    val privacyURL: String = CONFIGURATION_NOT_SET,
    val helpURL: String = CONFIGURATION_NOT_SET,
    val telegramURL: String = CONFIGURATION_NOT_SET,
    val whatsappURL: String = CONFIGURATION_NOT_SET,
    val aboutText: String = CONFIGURATION_NOT_SET,
    val silverPackagePrice: String = CONFIGURATION_NOT_SET,
    val goldPackagePrice: String = CONFIGURATION_NOT_SET,
    val diamondPackagePrice: String = CONFIGURATION_NOT_SET,
    val developedBy: String = CONFIGURATION_NOT_SET,
    val responseCode: Int = 0): Serializable
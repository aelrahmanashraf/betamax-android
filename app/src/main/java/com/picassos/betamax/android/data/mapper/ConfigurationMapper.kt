package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.ConfigurationDto
import com.picassos.betamax.android.domain.model.Configuration

fun ConfigurationDto.toConfiguration(): Configuration {
    return Configuration(
        email = app.configuration.email,
        privacyURL = app.configuration.privacyURL,
        helpURL = app.configuration.helpURL,
        telegramURL = app.configuration.telegramURL,
        whatsappURL = app.configuration.whatsappURL,
        aboutText = app.configuration.aboutText,
        silverPackagePrice = app.configuration.silverPackagePrice,
        goldPackagePrice = app.configuration.goldPackagePrice,
        diamondPackagePrice = app.configuration.diamondPackagePrice,
        developedBy = app.appBuild.developedBy,
        responseCode = app.responseCode.code)
}
package com.picassos.betamax.android.data.mapper.paypal

import com.picassos.betamax.android.data.source.remote.dto.paypal.PayPalAuthenticationDto
import com.picassos.betamax.android.domain.model.paypal.PayPalAuthentication

fun PayPalAuthenticationDto.toPayPalAuthentication(): PayPalAuthentication {
    return PayPalAuthentication(
        scope = scope,
        accessToken = accessToken,
        tokenType = tokenType,
        appId = appId,
        expiresIn = expiresIn,
        nonce = nonce)
}
package com.picassos.betamax.android.domain.model.paypal

data class PayPalCaptureAuthorizedOrder(
    val authentication: PayPalAuthentication,
    val captureOrder: PayPalCaptureOrder)
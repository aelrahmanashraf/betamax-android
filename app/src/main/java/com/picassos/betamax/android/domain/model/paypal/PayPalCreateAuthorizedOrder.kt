package com.picassos.betamax.android.domain.model.paypal

data class PayPalCreateAuthorizedOrder(
    val authentication: PayPalAuthentication,
    val order: PayPalCreateOrder)
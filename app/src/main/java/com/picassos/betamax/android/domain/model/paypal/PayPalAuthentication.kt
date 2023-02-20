package com.picassos.betamax.android.domain.model.paypal

data class PayPalAuthentication(
    val scope: String,
    val accessToken: String,
    val tokenType: String,
    val appId: String,
    val expiresIn: Int,
    val nonce: String)

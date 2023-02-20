package com.picassos.betamax.android.data.source.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PayPalAuthenticationDto(
    @SerializedName("scope")
    val scope: String,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("nonce")
    val nonce: String)
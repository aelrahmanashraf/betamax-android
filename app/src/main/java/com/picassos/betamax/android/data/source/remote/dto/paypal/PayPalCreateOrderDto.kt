package com.picassos.betamax.android.data.source.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PayPalCreateOrderDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("links")
    val links: List<Link>,
    @SerializedName("status")
    val status: String) {

    data class Link(
        @SerializedName("href")
        val href: String,
        @SerializedName("method")
        val method: String,
        @SerializedName("rel")
        val rel: String)
}
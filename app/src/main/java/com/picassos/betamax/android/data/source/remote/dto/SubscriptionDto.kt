package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubscriptionDto(
    @SerializedName("subscription")
    val subscription: Subscription) {

    data class Subscription(
        @SerializedName("details")
        val details: Details,
        @SerializedName("response")
        val responseCode: ResponseCode) {

        data class Details(
            @SerializedName("subscription_package")
            val subscriptionPackage: Int,
            @SerializedName("subscription_end")
            val subscriptionEnd: String,
            @SerializedName("days_left")
            val daysLeft: Int): Serializable

        data class ResponseCode(
            @SerializedName("code")
            val code: Int)
    }
}
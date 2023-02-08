package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class Subscription(
    val subscriptionPackage: Int,
    val subscriptionStart: String,
    val subscriptionEnd: String,
    val daysLeft: Int,
    val responseCode: Int): Serializable

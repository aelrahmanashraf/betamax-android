package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class Subscription(
    val subscriptionPackage: Int,
    val subscriptionEnd: String,
    val daysLeft: Int,
    val responseCode: Int): Serializable

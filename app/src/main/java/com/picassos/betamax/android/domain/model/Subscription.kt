package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class Subscription(
    val subscriptionPackage: Int = 0,
    val subscriptionEnd: String = "",
    val daysLeft: Int = 0,
    val responseCode: Int = 0): Serializable

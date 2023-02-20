package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class SubscriptionPackage(
    val id: Int = 0,
    val title: String = "",
    val price: String = ""): Serializable

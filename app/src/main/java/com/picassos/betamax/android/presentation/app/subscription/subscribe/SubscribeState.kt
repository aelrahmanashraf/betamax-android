package com.picassos.betamax.android.presentation.app.subscription.subscribe

data class SubscribeState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
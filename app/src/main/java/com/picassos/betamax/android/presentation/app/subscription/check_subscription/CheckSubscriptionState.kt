package com.picassos.betamax.android.presentation.app.subscription.check_subscription

import com.picassos.betamax.android.domain.model.Subscription

data class CheckSubscriptionState(
    val isLoading: Boolean = false,
    val response: Subscription? = null,
    val error: String? = null)
package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.SubscriptionDto
import com.picassos.betamax.android.domain.model.Subscription

fun SubscriptionDto.toSubscription(): Subscription {
    return Subscription(
        subscriptionPackage = subscription.details.subscriptionPackage,
        subscriptionEnd = subscription.details.subscriptionEnd,
        daysLeft = subscription.details.daysLeft,
        responseCode =  subscription.responseCode.code)
}

package com.picassos.betamax.android.domain.usecase.subscription

import com.picassos.betamax.android.domain.repository.SubscriptionRepository
import javax.inject.Inject

class SetLocalSubscriptionUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(subscription: String) =
        repository.setLocalSubscription(subscription)
}
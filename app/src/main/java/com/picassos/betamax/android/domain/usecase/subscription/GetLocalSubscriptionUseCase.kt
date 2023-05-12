package com.picassos.betamax.android.domain.usecase.subscription

import com.picassos.betamax.android.domain.model.Subscription
import com.picassos.betamax.android.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocalSubscriptionUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    operator fun invoke(): Flow<Subscription> =
        repository.getLocalSubscription()
}
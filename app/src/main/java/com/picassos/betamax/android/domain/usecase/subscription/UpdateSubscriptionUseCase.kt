package com.picassos.betamax.android.domain.usecase.subscription

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateSubscriptionUseCase @Inject constructor(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(token: String, subscriptionPackage: Int): Flow<Resource<Int>> =
        repository.updateSubscription(token, subscriptionPackage)
}
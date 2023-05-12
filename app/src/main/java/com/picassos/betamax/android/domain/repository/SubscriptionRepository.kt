package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getLocalSubscription(): Flow<Subscription>
    suspend fun setLocalSubscription(subscription: String)
    suspend fun checkSubscription(token: String): Flow<Resource<Subscription>>
    suspend fun updateSubscription(token: String, subscriptionPackage: Int): Flow<Resource<Int>>
}
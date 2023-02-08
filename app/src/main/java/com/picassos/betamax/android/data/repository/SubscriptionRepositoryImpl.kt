package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.toSubscription
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Subscription
import com.picassos.betamax.android.domain.repository.SubscriptionRepository
import com.picassos.betamax.android.core.utilities.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(private val service: APIService): SubscriptionRepository {
    override suspend fun checkSubscription(token: String): Flow<Resource<Subscription>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.checkSubscription(token = token)
                emit(Resource.Success(response.toSubscription()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun updateSubscription(token: String, subscriptionPackage: Int): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.updateSubscription(
                    token = token,
                    subscriptionPackage = subscriptionPackage)
                emit(Resource.Success(response))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }
}
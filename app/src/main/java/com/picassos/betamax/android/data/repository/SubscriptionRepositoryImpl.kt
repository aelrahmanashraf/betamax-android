package com.picassos.betamax.android.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Subscription
import com.picassos.betamax.android.domain.repository.SubscriptionRepository
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.data.mapper.toSubscription
import com.picassos.betamax.android.data.source.local.datastore.SharedDataManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(private val service: APIService, private val sharedData: SharedDataManager): SubscriptionRepository {
    override fun getLocalSubscription(): Flow<Subscription> {
        return sharedData.dataStore.data.map { preferences ->
            Gson().fromJson(preferences[stringPreferencesKey(SUBSCRIPTION_KEY)], Subscription::class.java) ?: Subscription()
        }
    }

    override suspend fun setLocalSubscription(subscription: String) {
        sharedData.dataStore.edit { settings ->
            settings[stringPreferencesKey(SUBSCRIPTION_KEY)] = subscription
        }
    }

    override suspend fun checkSubscription(token: String): Flow<Resource<Subscription>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.checkSubscription(
                    token = token)
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

    companion object {
        const val SUBSCRIPTION_KEY = "subscription"
    }
}
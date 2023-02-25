package com.picassos.betamax.android.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.toAccount
import com.picassos.betamax.android.data.source.local.datastore.SharedDataManager
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.repository.AccountRepository
import com.picassos.betamax.android.core.utilities.Response
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(private val service: APIService, private val sharedData: SharedDataManager): AccountRepository {
    override fun getLocalAccount(): Flow<Account> {
        return sharedData.dataStore.data.map { preferences ->
            Gson().fromJson(preferences[stringPreferencesKey(ACCOUNT_KEY)], Account::class.java) ?: Account()
        }
    }

    override suspend fun getAccount(token: String, imei: String): Flow<Resource<Account>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.account(
                    token = token,
                    imei = imei)
                emit(Resource.Success(response.toAccount()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun setLocalAccount(account: String) {
        Firebase.crashlytics.setUserId(
            Gson().fromJson(account, Account::class.java).token)

        sharedData.dataStore.edit { settings ->
            settings[stringPreferencesKey(ACCOUNT_KEY)] = account
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
    }
}
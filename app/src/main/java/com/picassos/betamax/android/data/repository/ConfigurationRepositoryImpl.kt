package com.picassos.betamax.android.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.local.datastore.SharedDataManager
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.core.utilities.Response
import com.google.gson.Gson
import com.picassos.betamax.android.data.mapper.toConfiguration
import com.picassos.betamax.android.domain.model.Configuration
import com.picassos.betamax.android.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationRepositoryImpl @Inject constructor(private val service: APIService, private val sharedData: SharedDataManager): ConfigurationRepository {
    override fun getLocalConfiguration(): Flow<Configuration> {
        return sharedData.dataStore.data.map { preferences ->
            Gson().fromJson(preferences[stringPreferencesKey(CONFIGURATION_KEY)], Configuration::class.java) ?: Configuration()
        }
    }

    override suspend fun getConfiguration(): Flow<Resource<Configuration>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.configuration()
                emit(Resource.Success(response.toConfiguration()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun setLocalConfiguration(configuration: String) {
        sharedData.dataStore.edit { settings ->
            settings[stringPreferencesKey(CONFIGURATION_KEY)] = configuration
        }
    }

    companion object {
        const val CONFIGURATION_KEY = "configuration"
    }
}
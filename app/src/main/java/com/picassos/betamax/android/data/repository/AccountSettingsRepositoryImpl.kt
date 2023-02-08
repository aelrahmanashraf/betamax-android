package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.repository.AccountSettingsRepository
import com.picassos.betamax.android.core.utilities.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountSettingsRepositoryImpl @Inject constructor(private val service: APIService): AccountSettingsRepository {
    override suspend fun updateProfileInfo(token: String, username: String): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.updateProfileInfo(
                    token = token,
                    username = username)
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

    override suspend fun getVideoQuality(token: String): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.videoQuality(token = token)
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

    override suspend fun updateVideoQuality(token: String, quality: Int): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.updateVideoQuality(
                    token = token,
                    quality = quality)
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
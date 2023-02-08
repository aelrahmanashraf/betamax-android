package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.repository.ContinueWatchingRepository
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.data.mapper.toContinueWatching
import com.picassos.betamax.android.domain.model.ContinueWatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContinueWatchingRepositoryImpl @Inject constructor(private val service: APIService): ContinueWatchingRepository {
    override suspend fun getContinueWatching(token: String): Flow<Resource<ContinueWatching>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.continueWatching(token = token)
                emit(Resource.Success(response.toContinueWatching()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun updateContinueWatching(token: String, contentId: Int, url: String, thumbnail: String, currentPosition: Int): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            delay(2000L)
            try {
                val response = service.updateContinueWatching(
                    token = token,
                    contentId = contentId,
                    url = url,
                    thumbnail = thumbnail,
                    currentPosition = currentPosition)
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

    override suspend fun deleteContinueWatching(token: String, contentId: Int): Flow<Resource<Int>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.deleteContinueWatching(
                    token = token,
                    contentId = contentId)
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
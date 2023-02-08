package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.toCast
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Cast
import com.picassos.betamax.android.domain.repository.CastRepository
import com.picassos.betamax.android.core.utilities.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastRepositoryImpl @Inject constructor(private val service: APIService): CastRepository {
    override suspend fun getCast(movieId: Int): Flow<Resource<Cast>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.cast(movieId = movieId)
                emit(Resource.Success(response.toCast()))
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
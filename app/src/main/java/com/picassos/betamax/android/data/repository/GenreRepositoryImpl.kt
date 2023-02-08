package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.toGenres
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.repository.GenreRepository
import com.picassos.betamax.android.core.utilities.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenreRepositoryImpl @Inject constructor(private val service: APIService): GenreRepository {
    override suspend fun getAllGenres(): Flow<Resource<Genres>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.allGenres()
                emit(Resource.Success(response.toGenres()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getHomeGenres(): Flow<Resource<Genres>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.homeGenres()
                emit(Resource.Success(response.toGenres()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getSpecialGenres(): Flow<Resource<Genres>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.specialGenres()
                emit(Resource.Success(response.toGenres()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getTvGenres(): Flow<Resource<Genres>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.tvGenres()
                emit(Resource.Success(response.toGenres()))
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
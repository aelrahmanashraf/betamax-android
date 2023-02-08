package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.toEpisodes
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.repository.SeriesRepository
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.data.mapper.toSeasons
import com.picassos.betamax.android.domain.model.Seasons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeriesRepositoryImpl @Inject constructor(private val service: APIService): SeriesRepository {
    override suspend fun getSeasons(movieId: Int): Flow<Resource<Seasons>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.seasons(movieId = movieId)
                emit(Resource.Success(response.toSeasons()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getEpisodes(movieId: Int, seasonLevel: Int): Flow<Resource<Episodes>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.episodes(
                    movieId = movieId,
                    seasonLevel = seasonLevel)
                emit(Resource.Success(response.toEpisodes()))
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
package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.*
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.domain.repository.TvChannelRepository
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.domain.model.ViewTvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TvChannelRepositoryImpl @Inject constructor(private val service: APIService): TvChannelRepository {
    override suspend fun getTvChannels(): Flow<Resource<TvChannels>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.tvChannels()
                emit(Resource.Success(response.toTvChannels()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getTvChannelsByGenre(genreId: Int): Flow<Resource<TvChannels>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.tvChannelsByGenre(genreId = genreId)
                emit(Resource.Success(response.toTvChannels()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getTvChannel(token: String, tvChannelId: Int): Flow<Resource<ViewTvChannel>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                coroutineScope {
                    val tvChannelDetails = async(Dispatchers.IO) {
                        service.tvChannel(tvChannelId = tvChannelId)
                    }
                    val tvGenres = async (Dispatchers.IO) {
                        service.tvGenres()
                    }
                    val relatedTvChannels = async(Dispatchers.IO) {
                        service.tvChannels()
                    }
                    val checkTvChannelSaved = async(Dispatchers.IO) {
                        service.checkTvChannelSaved(
                            token = token,
                            tvChannelId = tvChannelId)
                    }
                    val videoQuality = async(Dispatchers.IO) {
                        service.videoQuality(token = token)
                    }
                    emit(Resource.Success(ViewTvChannel(
                        tvChannelDetails.await().toTvChannels(),
                        tvGenres.await().toGenres(),
                        relatedTvChannels.await().toTvChannels(),
                        checkTvChannelSaved.await(),
                        videoQuality.await())))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getSingleTvChannel(tvChannelId: Int): Flow<Resource<TvChannels>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.tvChannel(tvChannelId = tvChannelId)
                emit(Resource.Success(response.toTvChannels()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun saveTvChannel(token: String, tvChannelId: Int): Flow<Resource<String>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.saveTvChannel(
                    token = token,
                    tvChannelId = tvChannelId)
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

    override suspend fun getSavedTvChannels(token: String): Flow<Resource<TvChannels>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.savedTvChannels(
                    token = token)
                emit(Resource.Success(response.toTvChannels()))
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
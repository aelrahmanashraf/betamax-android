package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.domain.model.ViewTvChannel
import kotlinx.coroutines.flow.Flow

interface TvChannelRepository {
    suspend fun getTvChannels(): Flow<Resource<TvChannels>>
    suspend fun getTvChannelsByGenre(genreId: Int): Flow<Resource<TvChannels>>
    suspend fun getTvChannel(token: String, tvChannelId: Int): Flow<Resource<ViewTvChannel>>
    suspend fun getSingleTvChannel(tvChannelId: Int): Flow<Resource<TvChannels>>
    suspend fun saveTvChannel(token: String, tvChannelId: Int): Flow<Resource<String>>
    suspend fun getSavedTvChannels(token: String): Flow<Resource<TvChannels>>
}
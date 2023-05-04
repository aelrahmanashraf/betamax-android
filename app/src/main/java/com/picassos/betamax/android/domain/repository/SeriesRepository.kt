package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.model.Seasons
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {
    suspend fun getSeasons(movieId: Int): Flow<Resource<Seasons>>
    suspend fun getEpisodes(token: String, movieId: Int, seasonLevel: Int): Flow<Resource<Episodes>>
}
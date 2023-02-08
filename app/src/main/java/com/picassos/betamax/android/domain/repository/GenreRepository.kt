package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Genres
import kotlinx.coroutines.flow.Flow

interface GenreRepository {
    suspend fun getAllGenres(): Flow<Resource<Genres>>
    suspend fun getHomeGenres(): Flow<Resource<Genres>>
    suspend fun getSpecialGenres(): Flow<Resource<Genres>>
    suspend fun getTvGenres(): Flow<Resource<Genres>>
}
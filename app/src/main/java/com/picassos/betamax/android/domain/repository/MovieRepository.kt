package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.model.ViewMovie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getMovies(): Flow<Resource<Movies>>
    suspend fun getMoviesByGenre(genreId: Int, filter: String): Flow<Resource<Movies>>
    suspend fun getRelatedMovies(movieId: Int): Flow<Resource<Movies>>
    suspend fun getMovie(token: String, movieId: Int, seasonLevel: Int, genreId: Int): Flow<Resource<ViewMovie>>
    suspend fun getHomeMovies(): Flow<Resource<Movies>>
    suspend fun getHomeSeries(): Flow<Resource<Movies>>
    suspend fun getFeaturedMovies(): Flow<Resource<Movies>>
    suspend fun getSeries(): Flow<Resource<Movies>>
    suspend fun getSavedMovies(token: String): Flow<Resource<Movies>>
    suspend fun searchMovies(query: String, filter: String): Flow<Resource<Movies>>
    suspend fun saveMovie(token: String, movieId: Int): Flow<Resource<String>>
    suspend fun getNewlyReleaseMovies(filter: String): Flow<Resource<Movies>>
    suspend fun getTrendingMovies(filter: String): Flow<Resource<Movies>>
}
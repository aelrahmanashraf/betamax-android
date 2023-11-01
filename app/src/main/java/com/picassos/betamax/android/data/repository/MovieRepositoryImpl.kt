package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.*
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.model.ViewMovie
import com.picassos.betamax.android.domain.repository.MovieRepository
import com.picassos.betamax.android.core.utilities.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepositoryImpl @Inject constructor(private val service: APIService): MovieRepository {
    override suspend fun getMovies(token: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.movies(token = token)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getSeries(): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.series()
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getFeaturedMovies(): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.featuredMovies()
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getHomeMovies(token: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.homeMovies(token = token)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getHomeSeries(): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.homeSeries()
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getRelatedMovies(token: String, movieId: Int): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.relatedMovies(
                    token = token,
                    movieId = movieId)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getMoviesByGenre(token: String, genreId: Int, filter: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.moviesByGenre(
                    token = token,
                    genreId = genreId,
                    filter = filter)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getMovie(token: String, movieId: Int, seasonLevel: Int, genreId: Int): Flow<Resource<ViewMovie>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                coroutineScope {
                    val movieDetails = async(Dispatchers.IO) {
                        service.movie(movieId = movieId)
                    }
                    val movieGenre = async(Dispatchers.IO) {
                        service.genre(genreId = genreId)
                    }
                    val movieCast = async(Dispatchers.IO) {
                        service.cast(movieId = movieId)
                    }
                    val checkMovieSaved = async(Dispatchers.IO) {
                        service.checkMovieSaved(
                            token = token,
                            movieId = movieId)
                    }
                    val relatedMovies = async(Dispatchers.IO) {
                        service.relatedMovies(
                            token = token,
                            movieId = movieId)
                    }
                    val seriesEpisodes = async(Dispatchers.IO) {
                        service.episodes(
                            token = token,
                            movieId = movieId,
                            seasonLevel = seasonLevel)
                    }
                    emit(Resource.Success(ViewMovie(
                        movieDetails.await().toMovies(),
                        movieGenre.await().toGenre(),
                        movieCast.await().toCast(),
                        checkMovieSaved.await(),
                        relatedMovies.await().toMovies(),
                        seriesEpisodes.await().toEpisodes())))
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

    override suspend fun getSavedMovies(token: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.savedMovies(token = token)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun searchMovies(query: String, filter: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.searchMovies(
                    query = query,
                    filter = filter)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun saveMovie(token: String, movieId: Int): Flow<Resource<String>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.saveMovie(
                    token = token,
                    movieId = movieId)
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

    override suspend fun getNewlyReleaseMovies(token: String, filter: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.newlyReleaseMovies(
                    token = token,
                    filter = filter)
                emit(Resource.Success(response.toMovies()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getTrendingMovies(token: String, filter: String): Flow<Resource<Movies>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.trendingMovies(
                    token = token,
                    filter = filter)
                emit(Resource.Success(response.toMovies()))
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
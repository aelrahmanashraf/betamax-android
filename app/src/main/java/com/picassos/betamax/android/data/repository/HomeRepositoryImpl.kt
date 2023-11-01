package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.mapper.*
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.domain.model.Home
import com.picassos.betamax.android.domain.repository.HomeRepository
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
class HomeRepositoryImpl @Inject constructor(private val service: APIService): HomeRepository {
    override suspend fun getHome(token: String): Flow<Resource<Home>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                coroutineScope {
                    val featuredMovies = async(Dispatchers.IO) {
                        service.featuredMovies()
                    }
                    val specialGenres = async(Dispatchers.IO) {
                        service.specialGenres()
                    }
                    val myList = async(Dispatchers.IO) {
                        service.homeSavedMovies(token = token)
                    }
                    val newlyReleaseMovies = async(Dispatchers.IO) {
                        service.newlyReleaseMovies(token = token)
                    }
                    val trendingMovies = async(Dispatchers.IO) {
                        service.homeTrendingMovies(token = token)
                    }
                    val continueWatching = async(Dispatchers.IO) {
                        service.continueWatching(token = token)
                    }
                    emit(Resource.Success(Home(
                        specialGenres.await().toGenres(),
                        featuredMovies.await().toMovies(),
                        myList.await().toMovies(),
                        newlyReleaseMovies.await().toMovies(),
                        trendingMovies.await().toMovies(),
                        continueWatching.await().toContinueWatching())))
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
}
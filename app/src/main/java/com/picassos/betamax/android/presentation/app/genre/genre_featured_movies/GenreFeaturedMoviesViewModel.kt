package com.picassos.betamax.android.presentation.app.genre.genre_featured_movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.movie.GenreFeaturedMoviesUseCases
import com.picassos.betamax.android.presentation.app.genre.genre_movies.GenreFeaturedMoviesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenreFeaturedMoviesViewModel @Inject constructor(app: Application, private val genreFeaturedMoviesUseCases: GenreFeaturedMoviesUseCases): AndroidViewModel(app) {
    private val _movies = MutableStateFlow(GenreFeaturedMoviesState())
    val movies = _movies.asStateFlow()

    fun requestSavedMovies() {
        viewModelScope.launch {
            genreFeaturedMoviesUseCases.getLocalAccountUseCase.invoke().collect { account ->
                genreFeaturedMoviesUseCases.getSavedMoviesUseCase.invoke(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _movies.emit(GenreFeaturedMoviesState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _movies.emit(GenreFeaturedMoviesState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _movies.emit(GenreFeaturedMoviesState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    fun requestMovies() {
        viewModelScope.launch {
            genreFeaturedMoviesUseCases.getMoviesUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun requestTrendingMovies(filter: String = "all") {
        viewModelScope.launch {
            genreFeaturedMoviesUseCases.getTrendingMoviesUseCase.invoke(filter).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun requestSeries() {
        viewModelScope.launch {
            genreFeaturedMoviesUseCases.getSeriesUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(GenreFeaturedMoviesState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
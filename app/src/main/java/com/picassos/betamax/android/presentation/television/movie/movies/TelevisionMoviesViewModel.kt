package com.picassos.betamax.android.presentation.television.movie.movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.movie.TelevisionMoviesUseCases
import com.picassos.betamax.android.presentation.app.genre.genres.GenresState
import com.picassos.betamax.android.presentation.app.movie.MovieState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelevisionMoviesViewModel @Inject constructor(app: Application, private val televisionMoviesUseCases: TelevisionMoviesUseCases): AndroidViewModel(app) {
    private val _genres = MutableStateFlow(GenresState())
    val genres = _genres.asStateFlow()

    fun requestGenres() {
        viewModelScope.launch {
            televisionMoviesUseCases.getAllGenresUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _genres.emit(GenresState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _genres.emit(GenresState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _genres.emit(GenresState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _movies = MutableStateFlow(MovieState())
    val movies = _movies.asStateFlow()

    fun requestNewlyReleaseMovies(filter: String = "all") {
        viewModelScope.launch {
            televisionMoviesUseCases.getNewlyReleaseMoviesUseCase.invoke(filter).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(MovieState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(MovieState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(MovieState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun requestTrendingMovies(filter: String = "all") {
        viewModelScope.launch {
            televisionMoviesUseCases.getTrendingMoviesUseCase.invoke(filter).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(MovieState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(MovieState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(MovieState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun requestMoviesByGenre(genreId: Int, filter: String = "all") {
        viewModelScope.launch {
            televisionMoviesUseCases.getMoviesByGenreUseCase.invoke(genreId, filter).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(MovieState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(MovieState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(MovieState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun requestSearchMovies(query: String, filter: String = "all") {
        viewModelScope.launch {
            televisionMoviesUseCases.searchMoviesUseCase.invoke(query, filter).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _movies.emit(MovieState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _movies.emit(MovieState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _movies.emit(MovieState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
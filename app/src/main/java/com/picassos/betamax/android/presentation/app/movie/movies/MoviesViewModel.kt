package com.picassos.betamax.android.presentation.app.movie.movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.movie.MoviesUseCases
import com.picassos.betamax.android.presentation.app.movie.MovieState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(app: Application, private val moviesUseCases: MoviesUseCases): AndroidViewModel(app) {
    private val _movies = MutableStateFlow(MovieState())
    val movies = _movies.asStateFlow()

    fun requestMovies() {
        viewModelScope.launch {
            moviesUseCases.getMoviesUseCase.invoke().collect { result ->
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
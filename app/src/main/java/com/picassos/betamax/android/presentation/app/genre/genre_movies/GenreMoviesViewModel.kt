package com.picassos.betamax.android.presentation.app.genre.genre_movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.usecase.movie.GenreMoviesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenreMoviesViewModel @Inject constructor(app: Application, private val genreMoviesUseCases: GenreMoviesUseCases): AndroidViewModel(app) {
    private val _genres = MutableStateFlow<Genres.Genre?>(null)
    val genre = _genres.asStateFlow()

    fun setGenre(genre: Genres.Genre) {
        _genres.tryEmit(genre)
    }

    private val _movies = MutableStateFlow(GenreMoviesState())
    val movies = _movies.asStateFlow()

    fun requestGenreMovies(genreId: Int, filter: String = "all") {
        viewModelScope.launch {
            genreMoviesUseCases.getLocalAccountUseCase.invoke().collect { account ->
                genreMoviesUseCases.getMoviesByGenreUseCase.invoke(account.token, genreId, filter).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _movies.emit(GenreMoviesState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _movies.emit(GenreMoviesState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _movies.emit(GenreMoviesState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
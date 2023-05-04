package com.picassos.betamax.android.presentation.television.movie.view_movie

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.usecase.movie.ViewMovieUseCases
import com.picassos.betamax.android.presentation.app.episode.episodes.EpisodesState
import com.picassos.betamax.android.presentation.app.movie.save_movie.SaveMovieState
import com.picassos.betamax.android.presentation.app.movie.view_movie.ViewMovieState
import com.picassos.betamax.android.presentation.app.season.seasons.SeasonsState
import com.picassos.betamax.android.presentation.app.subscription.check_subscription.CheckSubscriptionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelevisionViewMovieViewModel @Inject constructor(app: Application, private val viewMovieUseCases: ViewMovieUseCases): AndroidViewModel(app) {
    private val _movie = MutableStateFlow<Movies.Movie?>(null)
    val movie = _movie.asStateFlow()

    fun setMovie(movie: Movies.Movie) {
        _movie.tryEmit(movie)
    }

    private val _viewMovie = MutableStateFlow(ViewMovieState())
    val viewMovie = _viewMovie.asStateFlow()

    fun requestMovie(movieId: Int, seasonLevel: Int, genreId: Int) {
        viewModelScope.launch {
            viewMovieUseCases.getLocalAccountUseCase.invoke().collect { account ->
                viewMovieUseCases.getMovieUseCase(account.token, movieId, seasonLevel, genreId).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _viewMovie.emit(ViewMovieState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _viewMovie.emit(ViewMovieState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _viewMovie.emit(ViewMovieState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _seasons = MutableStateFlow(SeasonsState())
    val seasons = _seasons.asStateFlow()

    fun requestSeasons(movieId: Int) {
        viewModelScope.launch {
            viewMovieUseCases.getSeasonsUseCase(movieId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _seasons.emit(SeasonsState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _seasons.emit(SeasonsState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _seasons.emit(SeasonsState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _episodes = MutableStateFlow(EpisodesState())
    val episodes = _episodes.asStateFlow()

    fun requestEpisodes(movieId: Int, seasonLevel: Int) {
        viewModelScope.launch {
            viewMovieUseCases.getLocalAccountUseCase.invoke().collect { account ->
                viewMovieUseCases.getEpisodesUseCase(account.token, movieId, seasonLevel).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _episodes.emit(EpisodesState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _episodes.emit(EpisodesState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _episodes.emit(EpisodesState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _saveMovie = MutableStateFlow(SaveMovieState())
    val saveMovie = _saveMovie.asStateFlow()

    fun requestSaveMovie(movieId: Int) {
        viewModelScope.launch {
            viewMovieUseCases.getLocalAccountUseCase.invoke().collect { account ->
                viewMovieUseCases.saveMovieUseCase(account.token, movieId).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _saveMovie.emit(SaveMovieState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _saveMovie.emit(SaveMovieState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _saveMovie.emit(SaveMovieState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _checkSubscription = MutableStateFlow(CheckSubscriptionState())
    val checkSubscription = _checkSubscription.asStateFlow()

    fun requestCheckSubscription() {
        viewModelScope.launch {
            viewMovieUseCases.getLocalAccountUseCase.invoke().collect { account ->
                viewMovieUseCases.checkSubscriptionUseCase(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _checkSubscription.emit(CheckSubscriptionState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _checkSubscription.emit(CheckSubscriptionState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _checkSubscription.emit(CheckSubscriptionState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
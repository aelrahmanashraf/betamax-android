package com.picassos.betamax.android.presentation.television.mylist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.mylist.TelevisionMyListUseCases
import com.picassos.betamax.android.presentation.app.movie.MovieState
import com.picassos.betamax.android.presentation.app.tvchannel.tvchannels.TvChannelsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelevisionMyListViewModel @Inject constructor(app: Application, private val televisionMyListUseCases: TelevisionMyListUseCases): AndroidViewModel(app) {
    private val _movies = MutableStateFlow(MovieState())
    val movies = _movies.asStateFlow()

    fun requestSavedMovies() {
        viewModelScope.launch {
            televisionMyListUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionMyListUseCases.getSavedMoviesUseCase.invoke(account.token).collect { result ->
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

    private val _tvChannels = MutableStateFlow(TvChannelsState())
    val tvChannels = _tvChannels.asStateFlow()

    fun requestSavedTvChannels() {
        viewModelScope.launch {
            televisionMyListUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionMyListUseCases.getSavedTvChannelsUseCase.invoke(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _tvChannels.emit(TvChannelsState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _tvChannels.emit(TvChannelsState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _tvChannels.emit(TvChannelsState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
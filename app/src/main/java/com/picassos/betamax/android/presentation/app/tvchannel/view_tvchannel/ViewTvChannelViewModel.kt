package com.picassos.betamax.android.presentation.app.tvchannel.view_tvchannel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.tvchannel.ViewTvUseCases
import com.picassos.betamax.android.presentation.app.genre.genres.GenresState
import com.picassos.betamax.android.presentation.app.tvchannel.tvchannels.TvChannelsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewTvChannelViewModel @Inject constructor(app: Application, private val viewTvUseCases: ViewTvUseCases): AndroidViewModel(app) {
    private val _viewTvChannel = MutableStateFlow(ViewTvChannelState())
    val viewTvChannel = _viewTvChannel.asStateFlow()

    fun requestTvChannel(tvChannelId: Int) {
        viewModelScope.launch {
            viewTvUseCases.getLocalAccountUseCase.invoke().collect { account ->
                viewTvUseCases.getTvChannelUseCase(account.token, tvChannelId).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _viewTvChannel.emit(ViewTvChannelState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _viewTvChannel.emit(ViewTvChannelState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _viewTvChannel.emit(ViewTvChannelState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _tvGenres = MutableStateFlow(GenresState())
    val tvGenres = _tvGenres.asStateFlow()

    fun requestTvGenres() {
        viewModelScope.launch {
            viewTvUseCases.getTvGenresUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _tvGenres.emit(GenresState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _tvGenres.emit(GenresState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _tvGenres.emit(GenresState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _tvChannels = MutableStateFlow(TvChannelsState())
    val tvChannels = _tvChannels.asStateFlow()

    fun requestTvChannels() {
        viewModelScope.launch {
            viewTvUseCases.getTvChannelsUseCase().collect { result ->
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

    fun requestTvChannelsByGenre(genreId: Int) {
        viewModelScope.launch {
            viewTvUseCases.getTvChannelsByGenreUseCase(genreId).collect { result ->
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
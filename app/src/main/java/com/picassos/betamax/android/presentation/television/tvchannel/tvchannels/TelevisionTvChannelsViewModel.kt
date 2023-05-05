package com.picassos.betamax.android.presentation.television.tvchannel.tvchannels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.tvchannel.TelevisionTvChannelsUseCases
import com.picassos.betamax.android.presentation.app.genre.genres.GenresState
import com.picassos.betamax.android.presentation.app.quality.QualityState
import com.picassos.betamax.android.presentation.app.tvchannel.tvchannels.TvChannelsState
import com.picassos.betamax.android.presentation.television.tvchannel.save_tvchannel.SaveTvChannelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Navigations {
    HomeNavigation,
    FavoritesNavigation
}

@HiltViewModel
class TelevisionTvChannelsViewModel @Inject constructor(app: Application, private val televisionTvChannelsUseCases: TelevisionTvChannelsUseCases): AndroidViewModel(app) {
    private val _selectedNavigation = MutableStateFlow(Navigations.HomeNavigation)
    val selectedNavigation = _selectedNavigation.asStateFlow()

    fun setSelectedNavigation(navigation: Navigations) {
        _selectedNavigation.tryEmit(navigation)
    }

    private val _tvGenres = MutableStateFlow(GenresState())
    val tvGenres = _tvGenres.asStateFlow()

    fun requestTvGenres() {
        viewModelScope.launch {
            televisionTvChannelsUseCases.getTvGenresUseCase().collect { result ->
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
            televisionTvChannelsUseCases.getTvChannelsUseCase().collect { result ->
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
            televisionTvChannelsUseCases.getTvChannelsByGenreUseCase(genreId).collect { result ->
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

    private val _preferredVideoQuality = MutableStateFlow(QualityState())
    val preferredVideoQuality = _preferredVideoQuality.asStateFlow()

    fun requestPreferredVideoQuality() {
        viewModelScope.launch {
            televisionTvChannelsUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionTvChannelsUseCases.getVideoQualityUseCase(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _preferredVideoQuality.emit(QualityState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _preferredVideoQuality.emit(QualityState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _preferredVideoQuality.emit(QualityState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    fun requestSavedTvChannels() {
        viewModelScope.launch {
            televisionTvChannelsUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionTvChannelsUseCases.getSavedTvChannelsUseCase.invoke(account.token).collect { result ->
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

    private val _saveTvChannel = MutableStateFlow(SaveTvChannelState())
    val saveTvChannel = _saveTvChannel.asStateFlow()

    fun requestSaveTvChannel(tvChannelId: Int) {
        viewModelScope.launch {
            televisionTvChannelsUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionTvChannelsUseCases.saveTvChannelUseCase(account.token, tvChannelId).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _saveTvChannel.emit(SaveTvChannelState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _saveTvChannel.emit(SaveTvChannelState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _saveTvChannel.emit(SaveTvChannelState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
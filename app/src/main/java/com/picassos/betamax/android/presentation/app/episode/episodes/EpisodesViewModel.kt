package com.picassos.betamax.android.presentation.app.episode.episodes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.episode.EpisodesUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpisodesViewModel @Inject constructor(app: Application, private val episodesUseCases: EpisodesUseCases): AndroidViewModel(app) {
    private val _episodes = MutableStateFlow(EpisodesState())
    val episodes = _episodes.asStateFlow()

    fun requestEpisodes(movieId: Int, seasonLevel: Int) {
        viewModelScope.launch {
            episodesUseCases.getLocalAccountUseCase.invoke().collect { account ->
                episodesUseCases.getEpisodesUseCase(account.token, movieId, seasonLevel).collect { result ->
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
}
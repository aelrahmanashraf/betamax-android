package com.picassos.betamax.android.presentation.app.season.seasons

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Seasons
import com.picassos.betamax.android.domain.usecase.season.SeasonsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonsViewModel @Inject constructor(app: Application, private val seasonsUseCases: SeasonsUseCases): AndroidViewModel(app) {
    private val _selectedSeason = MutableStateFlow<Seasons.Season?>(null)
    val selectedSeason = _selectedSeason.asStateFlow()

    fun setSelectedSeason (season: Seasons.Season) {
        _selectedSeason.tryEmit(season)
    }

    private val _seasons = MutableStateFlow(SeasonsState())
    val seasons = _seasons.asStateFlow()

    fun requestSeasons(movieId: Int) {
        viewModelScope.launch {
            seasonsUseCases.getSeasonsUseCase(movieId).collect { result ->
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
}
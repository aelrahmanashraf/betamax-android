package com.picassos.betamax.android.presentation.app.series

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.movie.SeriesUseCases
import com.picassos.betamax.android.presentation.app.movie.MovieState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeriesViewModel @Inject constructor(app: Application, private val seriesUseCases: SeriesUseCases): AndroidViewModel(app) {
    private val _series = MutableStateFlow(MovieState())
    val series = _series.asStateFlow()

    fun requestSeries() {
        viewModelScope.launch {
            seriesUseCases.getSeriesUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _series.emit(MovieState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _series.emit(MovieState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _series.emit(MovieState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
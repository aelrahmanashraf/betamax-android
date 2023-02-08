package com.picassos.betamax.android.presentation.television.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.main.TelevisionMainUseCases
import com.picassos.betamax.android.presentation.television.movie.movies_slider.TelevisionMoviesSliderState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelevisionMainViewModel @Inject constructor(app: Application, private val televisionMainUseCases: TelevisionMainUseCases): AndroidViewModel(app) {
    private val _moviesSlider = MutableStateFlow(TelevisionMoviesSliderState())
    val moviesSlider = _moviesSlider.asStateFlow()

    fun requestFeaturedMovies() {
        viewModelScope.launch {
            televisionMainUseCases.getFeaturedMoviesUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _moviesSlider.emit(TelevisionMoviesSliderState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _moviesSlider.emit(TelevisionMoviesSliderState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _moviesSlider.emit(TelevisionMoviesSliderState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
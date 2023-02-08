package com.picassos.betamax.android.presentation.app.genre.genres

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.genre.GenresUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(app: Application, private val genresUseCases: GenresUseCases): AndroidViewModel(app) {
    private val _genres = MutableStateFlow(GenresState())
    val genres = _genres.asStateFlow()

    fun requestAllGenres() {
        viewModelScope.launch {
            genresUseCases.getAllGenresUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _genres.emit(GenresState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _genres.emit(GenresState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _genres.emit(GenresState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
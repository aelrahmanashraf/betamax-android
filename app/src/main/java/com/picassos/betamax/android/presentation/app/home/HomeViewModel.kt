package com.picassos.betamax.android.presentation.app.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.home.HomeUseCases
import com.picassos.betamax.android.presentation.app.genre.genres.GenresState
import com.picassos.betamax.android.presentation.app.genre.special_genres.SpecialGenresState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(app: Application, private val homeUseCases: HomeUseCases): AndroidViewModel(app) {
    private val _home = MutableStateFlow(HomeState())
    val home = _home.asStateFlow()

    fun requestHomeContent() {
        viewModelScope.launch {
            homeUseCases.getLocalAccountUseCase.invoke().collect { account ->
                homeUseCases.getHomeUseCase.invoke(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _home.emit(HomeState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _home.emit(HomeState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _home.emit(HomeState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _genres = MutableStateFlow(GenresState())
    val genres = _genres.asStateFlow()

    fun requestGenres() {
        viewModelScope.launch {
            homeUseCases.getHomeGenresUseCase.invoke().collect { result ->
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

    private val _specialGenres = MutableStateFlow(SpecialGenresState())
    val specialGenres = _specialGenres.asStateFlow()

    fun requestSpecialGenres() {
        viewModelScope.launch {
            homeUseCases.getSpecialGenresUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _specialGenres.emit(SpecialGenresState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _specialGenres.emit(SpecialGenresState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _specialGenres.emit(SpecialGenresState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
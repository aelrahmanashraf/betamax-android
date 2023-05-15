package com.picassos.betamax.android.presentation.television.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.main.TelevisionMainUseCases
import com.picassos.betamax.android.presentation.app.home.HomeState
import com.picassos.betamax.android.presentation.app.subscription.check_subscription.CheckSubscriptionState
import com.picassos.betamax.android.presentation.television.main.navigation_focus.TelevisionMainNavigationFocusState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelevisionMainViewModel @Inject constructor(app: Application, private val televisionMainUseCases: TelevisionMainUseCases): AndroidViewModel(app) {
    private val _navigation = MutableStateFlow(TelevisionMainNavigationFocusState())
    val navigation = _navigation.asStateFlow()

    fun setNavigationMoviesFocusState(focused: Boolean) {
        _navigation.update { state
            -> state.copy(isNavigationMoviesFocused = focused) }
    }

    fun setNavigationSeriesFocusState(focused: Boolean) {
        _navigation.update { state
            -> state.copy(isNavigationSeriesFocused = focused) }
    }

    fun setNavigationLiveTvFocusState(focused: Boolean) {
        _navigation.update { state
            -> state.copy(isNavigationLiveTvFocused = focused) }
    }

    fun setNavigationMyListFocusState(focused: Boolean) {
        _navigation.update { state
            -> state.copy(isNavigationMyListFocused = focused) }
    }

    fun setNavigationProfileFocusState(focused: Boolean) {
        _navigation.update { state
            -> state.copy(isNavigationProfileFocused = focused) }
    }

    private val _home = MutableStateFlow(HomeState())
    val home = _home.asStateFlow()

    fun requestHomeContent() {
        viewModelScope.launch {
            televisionMainUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionMainUseCases.getHomeUseCase.invoke(account.token).collect { result ->
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

    private val _checkSubscription = MutableStateFlow(CheckSubscriptionState())
    val checkSubscription = _checkSubscription.asStateFlow()

    fun requestCheckSubscription() {
        viewModelScope.launch {
            televisionMainUseCases.getLocalAccountUseCase.invoke().collect { account ->
                televisionMainUseCases.checkSubscriptionUseCase(account.token).collect { result ->
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
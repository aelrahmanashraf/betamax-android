package com.picassos.betamax.android.presentation.app.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.account.AccountUseCases
import com.picassos.betamax.android.presentation.app.auth.signout.SignoutState
import com.picassos.betamax.android.presentation.app.subscription.check_subscription.CheckSubscriptionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(app: Application, private val accountUseCases: AccountUseCases): AndroidViewModel(app) {
    private val _profile = MutableStateFlow(ProfileState())
    val profile = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            accountUseCases.getLocalAccountUseCase.invoke().collect { account ->
                _profile.emit(ProfileState(
                    response = account))
            }
        }
    }

    private val _checkSubscription = MutableStateFlow(CheckSubscriptionState())
    val checkSubscription = _checkSubscription.asStateFlow()

    fun requestCheckSubscription() {
        viewModelScope.launch {
            accountUseCases.getLocalAccountUseCase.invoke().collect { account ->
                accountUseCases.checkSubscriptionUseCase(account.token).collect { result ->
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

    private val _signout = MutableStateFlow(SignoutState())
    val signout = _signout.asStateFlow()

    fun requestSignout() {
        viewModelScope.launch {
            accountUseCases.getLocalAccountUseCase.invoke().collect { account ->
                accountUseCases.signoutUseCase(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _signout.emit(SignoutState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _signout.emit(SignoutState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _signout.emit(SignoutState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
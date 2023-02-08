package com.picassos.betamax.android.presentation.app.subscription.subscribe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.subscription.SubscribeUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscribeViewModel @Inject constructor(app: Application, private val subscribeUseCases: SubscribeUseCases): AndroidViewModel(app) {
    private val _subscribe = MutableStateFlow(SubscribeState())
    val subscribe = _subscribe.asStateFlow()

    fun requestUpdateSubscription(subscriptionPackage: Int) {
        viewModelScope.launch {
            subscribeUseCases.getLocalAccountUseCase.invoke().collect { account ->
                subscribeUseCases.updateSubscriptionUseCase(account.token, subscriptionPackage).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _subscribe.emit(SubscribeState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _subscribe.emit(SubscribeState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _subscribe.emit(SubscribeState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
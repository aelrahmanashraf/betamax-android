package com.picassos.betamax.android.presentation.app.auth.send_reset_email

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.send_reset_email.SendResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendResetEmailViewModel @Inject constructor(app: Application, private val sendResetEmailUseCase: SendResetEmailUseCase): AndroidViewModel(app) {
    private val _sendResetEmail = MutableStateFlow(SendResetEmailState())
    val sendResetEmail = _sendResetEmail.asStateFlow()

    fun requestSendResetEmail(email: String) {
        viewModelScope.launch {
            sendResetEmailUseCase(email).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _sendResetEmail.emit(SendResetEmailState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _sendResetEmail.emit(SendResetEmailState(
                            responseCode = result.data))
                    }
                    is Resource.Error -> {
                        _sendResetEmail.emit(SendResetEmailState(
                            error = result.message))
                    }
                }
            }
        }
    }
}

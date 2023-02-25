package com.picassos.betamax.android.presentation.app.auth.signin

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.usecase.signin.SigninUseCases
import com.google.gson.Gson
import com.picassos.betamax.android.core.utilities.Security
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SigninViewModel @Inject constructor(app: Application, private val signinUseCases: SigninUseCases): AndroidViewModel(app) {
    private val _signinFormState = MutableStateFlow(SigninFormState())
    val signinFormState = _signinFormState.asStateFlow()

    private fun onEvent(event: SigninFormEvent) {
        when (event) {
            is SigninFormEvent.EmailChanged -> {
                _signinFormState.update { state
                    -> state.copy(email = event.email) }
            }
            is SigninFormEvent.PasswordChanged -> {
                _signinFormState.update { state
                    -> state.copy(password = event.password) }
            }
            is SigninFormEvent.Submit -> {
                val emailResult = signinUseCases.signinValidation.emailValidation.execute(_signinFormState.value.email)
                val passwordResult = signinUseCases.signinValidation.passwordValidation.execute(_signinFormState.value.password)

                val hasError = listOf(
                    emailResult,
                    passwordResult
                ).any { result -> !result.successful }

                if (hasError) {
                    _signinFormState.update { state ->
                        state.copy(
                            emailError = emailResult.errorMessage,
                            passwordError = passwordResult.errorMessage) }
                    return
                }

                _signinFormState.update { state ->
                    state.copy(
                        emailError = null,
                        passwordError = null) }

                requestSignin(
                    email = _signinFormState.value.email,
                    password = _signinFormState.value.password)
            }
        }
    }

    private val _signin = MutableStateFlow(SigninState())
    val signin = _signin.asStateFlow()

    private fun requestSignin(imei: String = Security.getDeviceImei(), email: String, password: String) {
        viewModelScope.launch {
            signinUseCases.signinUseCase(imei, email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _signin.emit(SigninState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _signin.emit(SigninState(
                            response = result.data))

                        _signin.value.response?.let { account ->
                            if (account.responseCode == 200) {
                                signinUseCases.setLocalAccountUseCase(Gson().toJson(Account(
                                    token = account.token,
                                    username = account.username,
                                    emailAddress = account.emailAddress,
                                    emailConfirmed = account.emailConfirmed)))
                            }
                        }
                    }
                    is Resource.Error -> {
                        _signin.emit(SigninState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun onEmailChange(editable: Editable?) {
        onEvent(SigninFormEvent.EmailChanged(editable.toString()))
    }

    fun onPasswordChange(editable: Editable?) {
        onEvent(SigninFormEvent.PasswordChanged(editable.toString()))
    }

    fun onSubmit() {
        onEvent(SigninFormEvent.Submit)
    }
}

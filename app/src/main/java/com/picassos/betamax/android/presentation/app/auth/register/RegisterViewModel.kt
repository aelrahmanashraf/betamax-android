package com.picassos.betamax.android.presentation.app.auth.register

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.usecase.register.RegisterUseCases
import com.google.gson.Gson
import com.picassos.betamax.android.core.utilities.Security
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(app: Application, private val registerUseCases: RegisterUseCases): AndroidViewModel(app) {
    private val _registrationFormState = MutableStateFlow(RegistrationFormState())
    val registrationFormState = _registrationFormState.asStateFlow()

    private fun onEvent(event: RegistrationFormEvent) {
        when (event) {
            is RegistrationFormEvent.UsernameChanged -> {
                _registrationFormState.update { state
                    -> state.copy(username = event.username)
                }
            }
            is RegistrationFormEvent.EmailChanged -> {
                _registrationFormState.update { state
                    -> state.copy(email = event.email)
                }
            }
            is RegistrationFormEvent.PasswordChanged -> {
                _registrationFormState.update { state
                    -> state.copy(password = event.password)
                }
            }
            is RegistrationFormEvent.ConfirmPasswordChanged -> {
                _registrationFormState.update { state
                    -> state.copy(confirmPassword = event.confirmPassword)
                }
            }
            is RegistrationFormEvent.Submit -> {
                val usernameResult = registerUseCases.registerValidation.usernameValidation.execute(_registrationFormState.value.username)
                val emailResult = registerUseCases.registerValidation.emailValidation.execute(_registrationFormState.value.email)
                val passwordResult = registerUseCases.registerValidation.passwordValidation.execute(_registrationFormState.value.password)
                val confirmPasswordResult = registerUseCases.registerValidation.confirmPasswordValidation.execute(
                    _registrationFormState.value.password,
                    _registrationFormState.value.confirmPassword)

                val hasError = listOf(
                    usernameResult,
                    emailResult,
                    passwordResult,
                    confirmPasswordResult
                ).any { result -> !result.successful }

                if (hasError) {
                    _registrationFormState.update { state ->
                        state.copy(
                            usernameError = usernameResult.errorMessage,
                            emailError = emailResult.errorMessage,
                            passwordError = passwordResult.errorMessage,
                            confirmPasswordError = confirmPasswordResult.errorMessage)
                    }
                    return
                }

                _registrationFormState.update { state ->
                    state.copy(
                        usernameError = null,
                        emailError = null,
                        passwordError = null,
                        confirmPasswordError = null)
                }

                requestRegister(
                    username = _registrationFormState.value.username,
                    email = _registrationFormState.value.email,
                    password = _registrationFormState.value.password)
            }
        }
    }

    private val _register = MutableStateFlow(RegisterState())
    val register = _register.asStateFlow()

    private fun requestRegister(imei: String = Security.getDeviceImei(), username: String, email: String, password: String) {
        viewModelScope.launch {
            registerUseCases.registerUseCase.invoke(imei, username, email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _register.emit(RegisterState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _register.emit(RegisterState(
                            response = result.data))

                        _register.value.response?.let { account ->
                            if (account.responseCode == 200) {
                                registerUseCases.setLocalAccountUseCase(Gson().toJson(Account(
                                    token = account.token,
                                    username = account.username,
                                    emailAddress = account.emailAddress,
                                    emailConfirmed = account.emailConfirmed)))
                            }
                        }
                    }
                    is Resource.Error -> {
                        _register.emit(RegisterState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun onUsernameChanged(editable: Editable?) {
        onEvent(RegistrationFormEvent.UsernameChanged(editable.toString()))
    }

    fun onEmailChanged(editable: Editable?) {
        onEvent(RegistrationFormEvent.EmailChanged(editable.toString()))
    }

    fun onPasswordChanged(editable: Editable?) {
        onEvent(RegistrationFormEvent.PasswordChanged(editable.toString()))
    }

    fun onConfirmPasswordChanged(editable: Editable?) {
        onEvent(RegistrationFormEvent.ConfirmPasswordChanged(editable.toString()))
    }

    fun onSubmit() {
        onEvent(RegistrationFormEvent.Submit)
    }
}

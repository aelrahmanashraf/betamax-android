package com.picassos.betamax.android.presentation.app.auth.reset_password

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.usecase.reset_password.ResetPasswordUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(val app: Application, private val resetPasswordUseCases: ResetPasswordUseCases): AndroidViewModel(app) {
    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    fun setAccount(account: Account) {
        _account.tryEmit(account)
    }

    private val _resetPasswordFormState = MutableStateFlow(ResetPasswordFormState())
    val resetPasswordFormState = _resetPasswordFormState.asStateFlow()

    private fun onEvent(event: ResetPasswordFormEvent) {
        when (event) {
            is ResetPasswordFormEvent.PasswordChanged -> {
                _resetPasswordFormState.update { state
                    -> state.copy(password = event.password) }
            }
            is ResetPasswordFormEvent.ConfirmPasswordChanged -> {
                _resetPasswordFormState.update { state
                    -> state.copy(confirmPassword = event.confirmPassword) }
            }
            is ResetPasswordFormEvent.Validate -> {
                val passwordResult = resetPasswordUseCases.resetPasswordValidation.passwordValidation.execute(_resetPasswordFormState.value.password)
                val confirmPasswordResult = resetPasswordUseCases.resetPasswordValidation.confirmPasswordValidation.execute(
                    _resetPasswordFormState.value.password,
                    _resetPasswordFormState.value.confirmPassword)

                val hasError = listOf(
                    passwordResult,
                    confirmPasswordResult
                ).any { result -> !result.successful }

                if (hasError) {
                    _resetPasswordFormState.update { state ->
                        state.copy(
                            passwordError = passwordResult.errorMessage,
                            confirmPasswordError = confirmPasswordResult.errorMessage) }
                    return
                }

                _resetPasswordFormState.update { state ->
                    state.copy(
                        passwordError = null,
                        confirmPasswordError = null) }
            }
            is ResetPasswordFormEvent.Submit -> {
                if (_resetPasswordFormState.value.passwordError == null
                    && _resetPasswordFormState.value.confirmPasswordError == null) {
                        requestResetPassword(
                            _account.value!!.token,
                            _account.value!!.emailAddress,
                            _resetPasswordFormState.value.password,
                            _resetPasswordFormState.value.confirmPassword)
                }
            }
        }
    }

    private val _resetPassword = MutableStateFlow(ResetPasswordState())
    val resetPassword = _resetPassword.asStateFlow()

    private fun requestResetPassword(token: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            resetPasswordUseCases.resetPasswordUseCase.invoke(token, email, password, confirmPassword).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _resetPassword.emit(ResetPasswordState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _resetPassword.emit(ResetPasswordState(
                            responseCode = result.data))
                    }
                    is Resource.Error -> {
                        _resetPassword.emit(ResetPasswordState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun onPasswordChange(editable: Editable?) {
        onEvent(ResetPasswordFormEvent.PasswordChanged(editable.toString()))
        onEvent(ResetPasswordFormEvent.Validate)
    }

    fun onConfirmPasswordChange(editable: Editable?) {
        onEvent(ResetPasswordFormEvent.ConfirmPasswordChanged(editable.toString()))
        onEvent(ResetPasswordFormEvent.Validate)
    }

    fun onSubmit() {
        onEvent(ResetPasswordFormEvent.Submit)
    }
}

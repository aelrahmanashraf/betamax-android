package com.picassos.betamax.android.presentation.app.auth.change_password

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.change_password.ChangePasswordUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(app: Application, private val changePasswordUseCases: ChangePasswordUseCases): AndroidViewModel(app) {
    private val _changePasswordFormState = MutableStateFlow(ChangePasswordFormState())
    val changePasswordFormState = _changePasswordFormState.asStateFlow()

    private fun onEvent(event: ChangePasswordFormEvent) {
        when (event) {
            is ChangePasswordFormEvent.CurrentPasswordChanged -> {
                _changePasswordFormState.update { state
                    -> state.copy(currentPassword = event.currentPassword) }
            }
            is ChangePasswordFormEvent.NewPasswordChanged -> {
                _changePasswordFormState.update { state
                    -> state.copy(newPassword = event.newPassword) }
            }
            is ChangePasswordFormEvent.ConfirmPasswordChanged -> {
                _changePasswordFormState.update { state
                    -> state.copy(confirmPassword = event.confirmPassword) }
            }
            is ChangePasswordFormEvent.Validate -> {
                val currentPasswordResult = changePasswordUseCases.changePasswordValidation.currentPasswordValidation.execute(_changePasswordFormState.value.currentPassword)
                val newPasswordResult = changePasswordUseCases.changePasswordValidation.newPasswordValidation.execute(_changePasswordFormState.value.newPassword)
                val confirmPasswordResult = changePasswordUseCases.changePasswordValidation.confirmPasswordValidation.execute(
                    _changePasswordFormState.value.newPassword,
                    _changePasswordFormState.value.confirmPassword)

                val hasError = listOf(
                    currentPasswordResult,
                    newPasswordResult,
                    confirmPasswordResult
                ).any { result -> !result.successful }

                if (hasError) {
                    _changePasswordFormState.update { state ->
                        state.copy(
                            currentPasswordError = state.currentPasswordError,
                            newPasswordError = state.newPasswordError,
                            confirmPasswordError = state.confirmPasswordError) }
                    return
                }

                _changePasswordFormState.update { state ->
                    state.copy(
                        currentPasswordError = null,
                        newPasswordError = null,
                        confirmPasswordError = null) }
            }
            is ChangePasswordFormEvent.Submit -> {
                if (_changePasswordFormState.value.currentPasswordError == null
                    && _changePasswordFormState.value.newPasswordError == null
                    && _changePasswordFormState.value.confirmPasswordError == null) {
                    requestChangePassword(
                        _changePasswordFormState.value.currentPassword,
                        _changePasswordFormState.value.newPassword)
                }
            }
        }
    }

    private val _changePassword = MutableStateFlow(ChangePasswordState())
    val changePassword = _changePassword.asStateFlow()

    private fun requestChangePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            changePasswordUseCases.getLocalAccountUseCase.invoke().collect { account ->
                changePasswordUseCases.changePasswordUseCase.invoke(account.token, currentPassword, newPassword).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _changePassword.emit(ChangePasswordState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _changePassword.emit(ChangePasswordState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _changePassword.emit(ChangePasswordState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    fun onCurrentPasswordChange(editable: Editable?) {
        onEvent(ChangePasswordFormEvent.CurrentPasswordChanged(editable.toString()))
        onEvent(ChangePasswordFormEvent.Validate)
    }

    fun onNewPasswordChange(editable: Editable?) {
        onEvent(ChangePasswordFormEvent.NewPasswordChanged(editable.toString()))
        onEvent(ChangePasswordFormEvent.Validate)
    }

    fun onConfirmPasswordChange(editable: Editable?) {
        onEvent(ChangePasswordFormEvent.ConfirmPasswordChanged(editable.toString()))
        onEvent(ChangePasswordFormEvent.Validate)
    }

    fun onSubmit() {
        onEvent(ChangePasswordFormEvent.Submit)
    }
}

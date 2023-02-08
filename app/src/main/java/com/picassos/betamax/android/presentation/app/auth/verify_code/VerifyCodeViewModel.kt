package com.picassos.betamax.android.presentation.app.auth.verify_code

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.usecase.verify_code.VerifyCodeUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerifyCodeViewModel @Inject constructor(app: Application, private val verifyCodeUseCases: VerifyCodeUseCases): AndroidViewModel(app) {
    private val _account = MutableStateFlow<Account?>(null)
    val account = _account.asStateFlow()

    fun setAccount(account: Account) {
        _account.tryEmit(account)
    }

    private val _request = MutableStateFlow<String?>(null)
    val request = _request.asStateFlow()

    fun setRequest(request: String) {
        _request.tryEmit(request)
    }

    private val _verifyCodeFormState = MutableStateFlow(VerifyCodeFormState())
    val verifyCodeFormState = _verifyCodeFormState.asStateFlow()

    private fun onEvent(event: VerifyCodeFormEvent) {
        when (event) {
            is VerifyCodeFormEvent.VerificationCodeChanged -> {
                _verifyCodeFormState.update { state
                    -> state.copy(verificationCode = event.verificationCode) }
            }
            is VerifyCodeFormEvent.Validate -> {
                val verificationCodeResult = verifyCodeUseCases.verifyCodeValidation.verificationCodeValidation.execute(_verifyCodeFormState.value.verificationCode)

                val hasError = listOf(
                    verificationCodeResult
                ).any { result -> !result.successful }

                if (hasError) {
                    _verifyCodeFormState.update { state ->
                        state.copy(
                            verificationCodeError = verificationCodeResult.errorMessage) }
                    return
                }

                _verifyCodeFormState.update { state ->
                    state.copy(
                        verificationCodeError = null) }
            }
            is VerifyCodeFormEvent.Submit -> {
                if (_verifyCodeFormState.value.verificationCodeError == null) {
                    requestVerifyCode(
                        _request.value!!,
                        _account.value!!.emailAddress,
                        _verifyCodeFormState.value.verificationCode.toInt())
                }
            }
        }
    }

    private val _verifyCode = MutableStateFlow(VerifyCodeState())
    val verifyCode = _verifyCode.asStateFlow()

    private fun requestVerifyCode(request: String, email: String, verificationCode: Int) {
        viewModelScope.launch {
            verifyCodeUseCases.verifyCodeUseCase.invoke(request, email, verificationCode).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _verifyCode.emit(VerifyCodeState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _verifyCode.emit(VerifyCodeState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _verifyCode.emit(VerifyCodeState(
                            error = result.message))
                    }
                }
            }
        }
    }

    fun onVerificationCodeChange(editable: Editable?) {
        onEvent(VerifyCodeFormEvent.VerificationCodeChanged(editable.toString()))
        onEvent(VerifyCodeFormEvent.Validate)
    }

    fun onSubmit() {
        onEvent(VerifyCodeFormEvent.Submit)
    }
}

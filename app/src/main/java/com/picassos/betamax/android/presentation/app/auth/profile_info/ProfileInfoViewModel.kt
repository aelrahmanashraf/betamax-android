package com.picassos.betamax.android.presentation.app.auth.profile_info

import android.app.Application
import android.text.Editable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.google.gson.Gson
import com.picassos.betamax.android.domain.usecase.account.profile_info.ProfileInfoUseCases
import com.picassos.betamax.android.presentation.app.auth.profile_info.update_profile_info.UpdateProfileInfoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileInfoViewModel @Inject constructor(app: Application, private val profileInfoUseCases: ProfileInfoUseCases): AndroidViewModel(app) {
    private val _profileInfoFormState = MutableStateFlow(ProfileInfoFormState())
    val profileInfoFormState = _profileInfoFormState.asStateFlow()

    private fun onEvent(event: ProfileInfoFormEvent) {
        when (event) {
            is ProfileInfoFormEvent.UsernameChanged -> {
                _profileInfoFormState.update { state
                    -> state.copy(username = event.username) }
            }
            is ProfileInfoFormEvent.Submit -> {
                val usernameResult = profileInfoUseCases.profileInfoValidation.usernameValidation.execute(_profileInfoFormState.value.username)

                val hasError = listOf(
                    usernameResult,
                ).any { result -> !result.successful }

                if (hasError) {
                    _profileInfoFormState.update { state ->
                        state.copy(
                            usernameError = usernameResult.errorMessage) }
                    return
                }

                _profileInfoFormState.update { state ->
                    state.copy(
                        usernameError = null) }

                requestUpdateProfileInfo(
                    _profileInfoFormState.value.username)
            }
        }
    }

    private val _updateProfileInfo = MutableStateFlow(UpdateProfileInfoState())
    val updateProfileInfo = _updateProfileInfo.asStateFlow()

    private fun requestUpdateProfileInfo(username: String) {
        viewModelScope.launch {
            profileInfoUseCases.getLocalAccountUseCase.invoke().collect { account ->
                profileInfoUseCases.updateProfileInfoUseCase(account.token, username).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _updateProfileInfo.emit(UpdateProfileInfoState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _updateProfileInfo.emit(UpdateProfileInfoState(
                                responseCode = result.data))

                            _updateProfileInfo.value.responseCode?.let { responseCode ->
                                if (responseCode == 200) {
                                    profileInfoUseCases.setLocalAccountUseCase(Gson().toJson(Account(
                                        token = account.token,
                                        username = _profileInfoFormState.value.username,
                                        emailAddress = account.emailAddress,
                                        emailConfirmed = account.emailConfirmed)))
                                }
                            }
                        }
                        is Resource.Error -> {
                            _updateProfileInfo.emit(UpdateProfileInfoState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    fun onUsernameChange(editable: Editable?) {
        onEvent(ProfileInfoFormEvent.UsernameChanged(editable.toString()))
    }
    
    fun onSubmit() {
        onEvent(ProfileInfoFormEvent.Submit)
    }
}

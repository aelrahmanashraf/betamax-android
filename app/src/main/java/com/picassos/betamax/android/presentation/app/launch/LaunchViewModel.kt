package com.picassos.betamax.android.presentation.app.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.core.utilities.Security
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.model.Configuration
import com.picassos.betamax.android.domain.usecase.launch.LaunchUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(app: Application, private val launchUseCases: LaunchUseCases): AndroidViewModel(app) {
    private val _launch = MutableStateFlow(LaunchState())
    val launch = _launch.asStateFlow()

    fun requestLaunch(imei: String = Security.getDeviceImei()) {
        viewModelScope.launch {
            launchUseCases.getLocalAccountUseCase.invoke().collect { localAccount ->
                launchUseCases.launchUseCase.invoke(localAccount.token, imei).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _launch.emit(LaunchState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _launch.emit(LaunchState(
                                response = result.data))

                            result.data?.let { data ->
                                val configuration = data.configuration
                                if (configuration.responseCode == 200) {
                                    launchUseCases.setLocalConfigurationUseCase.invoke(Gson().toJson(Configuration(
                                        email = configuration.email,
                                        privacyURL = configuration.privacyURL,
                                        helpURL = configuration.helpURL,
                                        telegramURL = configuration.telegramURL,
                                        whatsappURL = configuration.whatsappURL,
                                        aboutText = configuration.aboutText,
                                        bannerImage = configuration.bannerImage,
                                        silverPackagePrice = configuration.silverPackagePrice,
                                        goldPackagePrice = configuration.goldPackagePrice,
                                        diamondPackagePrice = configuration.diamondPackagePrice,
                                        developedBy = configuration.developedBy)))
                                }

                                val account = data.account
                                if (account.responseCode == 200) {
                                    launchUseCases.setLocalAccountUseCase(Gson().toJson(Account(
                                        token = account.token,
                                        username = account.username,
                                        emailAddress = account.emailAddress,
                                        emailConfirmed = account.emailConfirmed)))
                                } else {
                                    launchUseCases.setLocalAccountUseCase(Gson().toJson(Account()))
                                }
                            }
                        }
                        is Resource.Error -> {
                            _launch.emit(LaunchState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
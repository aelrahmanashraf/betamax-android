package com.picassos.betamax.android.domain.usecase.launch

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.configuration.GetLocalConfigurationUseCase
import com.picassos.betamax.android.domain.usecase.configuration.SetLocalConfigurationUseCase
import com.picassos.betamax.android.domain.usecase.signout.SignoutUseCase
import javax.inject.Inject

data class LaunchUseCases @Inject constructor(
    val launchUseCase: LaunchUseCase,
    val getLocalConfigurationUseCase: GetLocalConfigurationUseCase,
    val setLocalConfigurationUseCase: SetLocalConfigurationUseCase,
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val setLocalAccountUseCase: SetLocalAccountUseCase,
    val signoutUseCase: SignoutUseCase)
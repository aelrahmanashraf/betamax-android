package com.picassos.betamax.android.domain.usecase.register

import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import javax.inject.Inject

data class RegisterUseCases @Inject constructor(
    val setLocalAccountUseCase: SetLocalAccountUseCase,
    val registerUseCase: RegisterUseCase,
    val registerValidation: RegisterValidation)
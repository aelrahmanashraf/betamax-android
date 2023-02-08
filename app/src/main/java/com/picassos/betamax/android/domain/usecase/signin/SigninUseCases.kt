package com.picassos.betamax.android.domain.usecase.signin

import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import javax.inject.Inject

data class SigninUseCases @Inject constructor(
    val setLocalAccountUseCase: SetLocalAccountUseCase,
    val signinUseCase: SigninUseCase,
    val signinValidation: SigninValidation)
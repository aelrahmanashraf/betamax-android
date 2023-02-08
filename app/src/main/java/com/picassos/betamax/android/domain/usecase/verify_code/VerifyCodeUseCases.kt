package com.picassos.betamax.android.domain.usecase.verify_code

import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import javax.inject.Inject

data class VerifyCodeUseCases @Inject constructor(
    val setLocalAccountUseCase: SetLocalAccountUseCase,
    val verifyCodeUseCase: VerifyCodeUseCase,
    val verifyCodeValidation: VerifyCodeValidation)
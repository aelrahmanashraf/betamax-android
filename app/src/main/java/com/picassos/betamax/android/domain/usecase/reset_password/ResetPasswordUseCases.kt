package com.picassos.betamax.android.domain.usecase.reset_password

import javax.inject.Inject

data class ResetPasswordUseCases @Inject constructor(
    val resetPasswordUseCase: ResetPasswordUseCase,
    val resetPasswordValidation: ResetPasswordValidation)
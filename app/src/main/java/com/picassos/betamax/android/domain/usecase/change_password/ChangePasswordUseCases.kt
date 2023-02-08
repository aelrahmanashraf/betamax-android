package com.picassos.betamax.android.domain.usecase.change_password

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class ChangePasswordUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val changePasswordUseCase: ChangePasswordUseCase,
    val changePasswordValidation: ChangePasswordValidation)
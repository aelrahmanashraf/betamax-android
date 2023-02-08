package com.picassos.betamax.android.domain.usecase.reset_password

import com.picassos.betamax.android.domain.usecase.form_validation.ValidateConfirmPassword
import com.picassos.betamax.android.domain.usecase.form_validation.ValidatePassword
import javax.inject.Inject

data class ResetPasswordValidation @Inject constructor(
    val passwordValidation: ValidatePassword,
    val confirmPasswordValidation: ValidateConfirmPassword)
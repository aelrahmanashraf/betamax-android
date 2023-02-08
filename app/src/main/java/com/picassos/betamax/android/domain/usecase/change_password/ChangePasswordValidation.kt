package com.picassos.betamax.android.domain.usecase.change_password

import com.picassos.betamax.android.domain.usecase.form_validation.ValidateConfirmPassword
import com.picassos.betamax.android.domain.usecase.form_validation.ValidatePassword
import javax.inject.Inject

data class ChangePasswordValidation @Inject constructor(
    val currentPasswordValidation: ValidatePassword,
    val newPasswordValidation: ValidatePassword,
    val confirmPasswordValidation: ValidateConfirmPassword)
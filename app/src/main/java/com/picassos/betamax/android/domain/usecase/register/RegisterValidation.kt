package com.picassos.betamax.android.domain.usecase.register

import com.picassos.betamax.android.domain.usecase.form_validation.ValidateConfirmPassword
import com.picassos.betamax.android.domain.usecase.form_validation.ValidateEmail
import com.picassos.betamax.android.domain.usecase.form_validation.ValidatePassword
import com.picassos.betamax.android.domain.usecase.form_validation.ValidateUsername
import javax.inject.Inject

data class RegisterValidation @Inject constructor(
    val usernameValidation: ValidateUsername,
    val emailValidation: ValidateEmail,
    val passwordValidation: ValidatePassword,
    val confirmPasswordValidation: ValidateConfirmPassword)
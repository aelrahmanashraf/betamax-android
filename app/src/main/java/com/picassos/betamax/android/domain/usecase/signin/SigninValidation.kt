package com.picassos.betamax.android.domain.usecase.signin

import com.picassos.betamax.android.domain.usecase.form_validation.ValidateEmail
import com.picassos.betamax.android.domain.usecase.form_validation.ValidatePassword
import javax.inject.Inject

data class SigninValidation @Inject constructor(
    val emailValidation: ValidateEmail,
    val passwordValidation: ValidatePassword)
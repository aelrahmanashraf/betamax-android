package com.picassos.betamax.android.presentation.app.auth.reset_password

data class ResetPasswordFormState(
    val password: String = "",
    val passwordError: String? = "",
    val confirmPassword: String = "",
    val confirmPasswordError: String? = "")
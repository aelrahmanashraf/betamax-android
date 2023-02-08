package com.picassos.betamax.android.presentation.app.auth.signin

data class SigninFormState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null)
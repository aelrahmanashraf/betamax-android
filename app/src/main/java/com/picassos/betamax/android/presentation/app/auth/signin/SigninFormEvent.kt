package com.picassos.betamax.android.presentation.app.auth.signin

sealed class SigninFormEvent {
    data class EmailChanged(val email: String): SigninFormEvent()
    data class PasswordChanged(val password: String): SigninFormEvent()
    object Submit: SigninFormEvent()
}
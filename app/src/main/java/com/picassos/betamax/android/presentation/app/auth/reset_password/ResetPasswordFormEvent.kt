package com.picassos.betamax.android.presentation.app.auth.reset_password

sealed class ResetPasswordFormEvent {
    data class PasswordChanged(val password: String): ResetPasswordFormEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String): ResetPasswordFormEvent()
    object Validate: ResetPasswordFormEvent()
    object Submit: ResetPasswordFormEvent()
}
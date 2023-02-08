package com.picassos.betamax.android.presentation.app.auth.change_password

sealed class ChangePasswordFormEvent {
    data class CurrentPasswordChanged(val currentPassword: String): ChangePasswordFormEvent()
    data class NewPasswordChanged(val newPassword: String): ChangePasswordFormEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String): ChangePasswordFormEvent()
    object Validate: ChangePasswordFormEvent()
    object Submit: ChangePasswordFormEvent()
}

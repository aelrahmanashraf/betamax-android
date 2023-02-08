package com.picassos.betamax.android.presentation.app.auth.register

sealed class RegistrationFormEvent {
    class UsernameChanged(val username: String): RegistrationFormEvent()
    class EmailChanged(val email: String): RegistrationFormEvent()
    class PasswordChanged(val password: String): RegistrationFormEvent()
    class ConfirmPasswordChanged(val confirmPassword: String): RegistrationFormEvent()
    object Submit: RegistrationFormEvent()
}
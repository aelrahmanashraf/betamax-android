package com.picassos.betamax.android.presentation.app.auth.send_reset_email

sealed class SendResetEmailFormEvent {
    data class EmailChanged(val email: String): SendResetEmailFormEvent()
    object Submit: SendResetEmailFormEvent()
}
package com.picassos.betamax.android.presentation.app.auth.send_reset_email

data class SendResetEmailFormState(
    val email: String = "",
    val emailError: String? = null)
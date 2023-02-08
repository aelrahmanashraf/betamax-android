package com.picassos.betamax.android.presentation.app.auth.send_reset_email

data class SendResetEmailState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
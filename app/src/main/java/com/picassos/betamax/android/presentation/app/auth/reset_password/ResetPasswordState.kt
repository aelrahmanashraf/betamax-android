package com.picassos.betamax.android.presentation.app.auth.reset_password

data class ResetPasswordState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
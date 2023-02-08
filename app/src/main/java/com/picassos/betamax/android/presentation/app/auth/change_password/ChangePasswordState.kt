package com.picassos.betamax.android.presentation.app.auth.change_password

data class ChangePasswordState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
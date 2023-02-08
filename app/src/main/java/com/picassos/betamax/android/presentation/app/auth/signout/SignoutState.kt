package com.picassos.betamax.android.presentation.app.auth.signout

data class SignoutState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
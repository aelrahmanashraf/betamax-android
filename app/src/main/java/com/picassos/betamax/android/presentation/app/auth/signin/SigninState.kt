package com.picassos.betamax.android.presentation.app.auth.signin

import com.picassos.betamax.android.domain.model.Account

data class SigninState(
    val isLoading: Boolean = false,
    val response: Account? = null,
    val error: String? = null)
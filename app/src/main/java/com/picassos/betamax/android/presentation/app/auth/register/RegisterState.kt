package com.picassos.betamax.android.presentation.app.auth.register

import com.picassos.betamax.android.domain.model.Account

data class RegisterState(
    val isLoading: Boolean = false,
    val response: Account? = null,
    val error: String? = null)
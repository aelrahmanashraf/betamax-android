package com.picassos.betamax.android.presentation.app.auth.verify_code

import com.picassos.betamax.android.domain.model.Account

data class VerifyCodeState(
    val isLoading: Boolean = false,
    val response: Account? = null,
    val error: String? = null)
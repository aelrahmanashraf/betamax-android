package com.picassos.betamax.android.presentation.app.profile

import com.picassos.betamax.android.domain.model.Account

data class ProfileState(
    val isLoading: Boolean = false,
    val response: Account? = null,
    val error: String? = null)
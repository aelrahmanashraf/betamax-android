package com.picassos.betamax.android.presentation.app.launch

import com.picassos.betamax.android.domain.model.Launch

data class LaunchState(
    val isLoading: Boolean = false,
    val response: Launch? = null,
    val error: String? = null)
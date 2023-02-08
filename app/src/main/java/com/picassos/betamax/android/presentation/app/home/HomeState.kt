package com.picassos.betamax.android.presentation.app.home

import com.picassos.betamax.android.domain.model.Home

data class HomeState(
    val isLoading: Boolean = false,
    val response: Home? = null,
    val error: String? = null)
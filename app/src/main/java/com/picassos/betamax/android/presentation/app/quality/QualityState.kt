package com.picassos.betamax.android.presentation.app.quality

data class QualityState(
    val isLoading: Boolean = false,
    val response: Int? = null,
    val error: String? = null)
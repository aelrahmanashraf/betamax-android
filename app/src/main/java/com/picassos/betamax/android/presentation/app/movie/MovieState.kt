package com.picassos.betamax.android.presentation.app.movie

import com.picassos.betamax.android.domain.model.Movies

data class MovieState(
    val isLoading: Boolean = false,
    val response: Movies? = null,
    val error: String? = null)
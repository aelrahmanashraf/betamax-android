package com.picassos.betamax.android.presentation.app.genre.genres

import com.picassos.betamax.android.domain.model.Genres

data class GenresState(
    val isLoading: Boolean = false,
    val response: Genres? = null,
    val error: String? = null)
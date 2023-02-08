package com.picassos.betamax.android.presentation.app.genre.special_genres

import com.picassos.betamax.android.domain.model.Genres

data class SpecialGenresState(
    val isLoading: Boolean = false,
    val response: Genres? = null,
    val error: String? = null)
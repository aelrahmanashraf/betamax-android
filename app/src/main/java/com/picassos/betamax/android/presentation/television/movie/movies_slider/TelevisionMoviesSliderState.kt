package com.picassos.betamax.android.presentation.television.movie.movies_slider

import com.picassos.betamax.android.domain.model.Movies

data class TelevisionMoviesSliderState(
    val isLoading: Boolean = false,
    val response: Movies? = null,
    val error: String? = null)
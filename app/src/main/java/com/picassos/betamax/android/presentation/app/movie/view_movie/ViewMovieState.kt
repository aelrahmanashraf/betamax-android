package com.picassos.betamax.android.presentation.app.movie.view_movie

import com.picassos.betamax.android.domain.model.ViewMovie

data class ViewMovieState(
    val isLoading: Boolean = false,
    val response: ViewMovie? = null,
    val error: String? = null)
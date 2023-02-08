package com.picassos.betamax.android.presentation.app.movie.save_movie

data class SaveMovieState(
    val isLoading: Boolean = false,
    val responseCode: String? = null,
    val error: String? = null)
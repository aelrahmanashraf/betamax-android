package com.picassos.betamax.android.presentation.app.genre.genre_movies

import com.picassos.betamax.android.domain.model.Movies

data class GenreMoviesState(
    val isLoading: Boolean = false,
    val response: Movies? = null,
    val error: String? = null)
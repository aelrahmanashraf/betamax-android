package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class MoviePlayerContent(
    val movie: Movies.Movie,
    val currentPosition: Int = 0): Serializable
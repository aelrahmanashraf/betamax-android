package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class EpisodePlayerContent(
    val movie: Movies.Movie,
    val episode: Episodes.Episode,
    val currentPosition: Int = 0,
    val episodes: Episodes? = null): Serializable
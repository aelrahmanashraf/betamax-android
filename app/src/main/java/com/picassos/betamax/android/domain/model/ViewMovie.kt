package com.picassos.betamax.android.domain.model

data class ViewMovie(
    val movieDetails: Movies,
    val movieGenre: Genre,
    val movieCast: Cast,
    val movieSaved: Int,
    val relatedMovies: Movies,
    val movieEpisodes: Episodes)
package com.picassos.betamax.android.domain.model

data class Home(
    val genres: Genres,
    val featuredMovies: Movies,
    val myList: Movies,
    val newlyRelease: Movies,
    val trendingMovies: Movies,
    val continueWatching: ContinueWatching)
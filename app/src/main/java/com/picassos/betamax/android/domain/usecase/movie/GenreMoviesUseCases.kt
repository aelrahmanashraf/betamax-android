package com.picassos.betamax.android.domain.usecase.movie

import javax.inject.Inject

data class GenreMoviesUseCases @Inject constructor(
    val getMoviesByGenreUseCase: GetMoviesByGenreUseCase)
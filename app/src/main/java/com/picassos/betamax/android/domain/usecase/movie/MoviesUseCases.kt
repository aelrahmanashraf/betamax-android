package com.picassos.betamax.android.domain.usecase.movie

import javax.inject.Inject

data class MoviesUseCases @Inject constructor(
    val getMoviesUseCase: GetMoviesUseCase)

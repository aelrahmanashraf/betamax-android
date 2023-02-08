package com.picassos.betamax.android.domain.usecase.movie

import javax.inject.Inject

data class SearchUseCases @Inject constructor(
    val searchMoviesUseCase: SearchMoviesUseCase)
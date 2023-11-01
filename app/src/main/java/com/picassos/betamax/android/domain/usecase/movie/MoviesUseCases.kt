package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class MoviesUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getMoviesUseCase: GetMoviesUseCase)

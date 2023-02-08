package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class GenreFeaturedMoviesUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getMoviesUseCase: GetMoviesUseCase,
    val getSeriesUseCase: GetSeriesUseCase,
    val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    val getSavedMoviesUseCase: GetSavedMoviesUseCase)
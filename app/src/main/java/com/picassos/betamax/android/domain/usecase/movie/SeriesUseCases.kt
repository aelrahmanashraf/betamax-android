package com.picassos.betamax.android.domain.usecase.movie

import javax.inject.Inject

data class SeriesUseCases @Inject constructor(
    val getSeriesUseCase: GetSeriesUseCase)

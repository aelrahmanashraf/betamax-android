package com.picassos.betamax.android.domain.usecase.season

import javax.inject.Inject

data class SeasonsUseCases @Inject constructor(
    val getSeasonsUseCase: GetSeasonsUseCase)
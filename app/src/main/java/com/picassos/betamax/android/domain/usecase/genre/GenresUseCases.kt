package com.picassos.betamax.android.domain.usecase.genre

import javax.inject.Inject

data class GenresUseCases @Inject constructor(
    val getAllGenresUseCase: GetAllGenresUseCase)
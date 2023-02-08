package com.picassos.betamax.android.domain.usecase.home

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.genre.GetHomeGenresUseCase
import com.picassos.betamax.android.domain.usecase.genre.GetSpecialGenresUseCase
import javax.inject.Inject

data class HomeUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getHomeUseCase: GetHomeUseCase,
    val getHomeGenresUseCase: GetHomeGenresUseCase,
    val getSpecialGenresUseCase: GetSpecialGenresUseCase)
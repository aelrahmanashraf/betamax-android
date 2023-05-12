package com.picassos.betamax.android.domain.usecase.main

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.home.GetHomeUseCase
import com.picassos.betamax.android.domain.usecase.movie.GetFeaturedMoviesUseCase
import com.picassos.betamax.android.domain.usecase.movie.GetNewlyReleaseMoviesUseCase
import javax.inject.Inject

data class TelevisionMainUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getHomeUseCase: GetHomeUseCase,
    val getNewlyReleaseMoviesUseCase: GetNewlyReleaseMoviesUseCase,
    val getFeaturedMoviesUseCase: GetFeaturedMoviesUseCase)

package com.picassos.betamax.android.domain.usecase.main

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.home.GetHomeUseCase
import com.picassos.betamax.android.domain.usecase.movie.GetFeaturedMoviesUseCase
import com.picassos.betamax.android.domain.usecase.movie.GetMovieUseCase
import com.picassos.betamax.android.domain.usecase.movie.GetNewlyReleaseMoviesUseCase
import com.picassos.betamax.android.domain.usecase.subscription.CheckSubscriptionUseCase
import javax.inject.Inject

data class TelevisionMainUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val checkSubscriptionUseCase: CheckSubscriptionUseCase,
    val getHomeUseCase: GetHomeUseCase,
    val getNewlyReleaseMoviesUseCase: GetNewlyReleaseMoviesUseCase,
    val getFeaturedMoviesUseCase: GetFeaturedMoviesUseCase)

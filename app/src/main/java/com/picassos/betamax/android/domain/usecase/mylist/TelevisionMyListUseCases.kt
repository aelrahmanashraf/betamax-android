package com.picassos.betamax.android.domain.usecase.mylist

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.movie.GetSavedMoviesUseCase
import com.picassos.betamax.android.domain.usecase.tvchannel.GetSavedTvChannelsUseCase
import javax.inject.Inject

data class TelevisionMyListUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getSavedMoviesUseCase: GetSavedMoviesUseCase,
    val getSavedTvChannelsUseCase: GetSavedTvChannelsUseCase)
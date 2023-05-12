package com.picassos.betamax.android.domain.usecase.tvchannel

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class TvUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getTvChannelsUseCase: GetTvChannelsUseCase)
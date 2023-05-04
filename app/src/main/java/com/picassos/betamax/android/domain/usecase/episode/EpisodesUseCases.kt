package com.picassos.betamax.android.domain.usecase.episode

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.subscription.CheckSubscriptionUseCase
import javax.inject.Inject

data class EpisodesUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getEpisodesUseCase: GetEpisodesUseCase)
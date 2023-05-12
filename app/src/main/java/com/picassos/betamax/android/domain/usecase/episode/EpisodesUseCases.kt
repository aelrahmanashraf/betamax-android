package com.picassos.betamax.android.domain.usecase.episode

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class EpisodesUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getEpisodesUseCase: GetEpisodesUseCase)
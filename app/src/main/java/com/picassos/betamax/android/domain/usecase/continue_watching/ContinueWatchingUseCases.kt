package com.picassos.betamax.android.domain.usecase.continue_watching

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class ContinueWatchingUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getContinueWatchingUseCase: GetContinueWatchingUseCase,
    val updateContinueWatchingUseCase: UpdateContinueWatchingUseCase,
    val deleteContinueWatchingUseCase: DeleteContinueWatchingUseCase)
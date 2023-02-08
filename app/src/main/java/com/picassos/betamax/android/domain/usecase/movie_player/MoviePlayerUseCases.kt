package com.picassos.betamax.android.domain.usecase.movie_player

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.continue_watching.DeleteContinueWatchingUseCase
import com.picassos.betamax.android.domain.usecase.continue_watching.UpdateContinueWatchingUseCase
import javax.inject.Inject

data class MoviePlayerUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val updateContinueWatchingUseCase: UpdateContinueWatchingUseCase,
    val deleteContinueWatchingUseCase: DeleteContinueWatchingUseCase)
package com.picassos.betamax.android.domain.usecase.subscription

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class SubscribeUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val updateSubscriptionUseCase: UpdateSubscriptionUseCase)
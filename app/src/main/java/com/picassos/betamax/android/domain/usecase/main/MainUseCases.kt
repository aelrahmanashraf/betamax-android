package com.picassos.betamax.android.domain.usecase.main

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.subscription.CheckSubscriptionUseCase
import javax.inject.Inject

data class MainUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val checkSubscriptionUseCase: CheckSubscriptionUseCase)

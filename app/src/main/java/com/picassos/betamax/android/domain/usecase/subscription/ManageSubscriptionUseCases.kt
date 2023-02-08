package com.picassos.betamax.android.domain.usecase.subscription

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class ManageSubscriptionUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val checkSubscriptionUseCase: CheckSubscriptionUseCase)
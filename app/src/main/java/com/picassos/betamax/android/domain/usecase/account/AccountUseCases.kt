package com.picassos.betamax.android.domain.usecase.account

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.signout.SignoutUseCase
import com.picassos.betamax.android.domain.usecase.subscription.CheckSubscriptionUseCase
import com.picassos.betamax.android.domain.usecase.subscription.SetLocalSubscriptionUseCase
import javax.inject.Inject

data class AccountUseCases @Inject constructor(
    val getAccountUseCase: GetAccountUseCase,
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val setLocalAccountUseCase: SetLocalAccountUseCase,
    val checkSubscriptionUseCase: CheckSubscriptionUseCase,
    val setLocalSubscriptionUseCase: SetLocalSubscriptionUseCase,
    val signoutUseCase: SignoutUseCase)
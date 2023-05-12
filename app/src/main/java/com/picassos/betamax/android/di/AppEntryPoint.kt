package com.picassos.betamax.android.di

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.account.auth.SetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.configuration.GetLocalConfigurationUseCase
import com.picassos.betamax.android.domain.usecase.configuration.SetLocalConfigurationUseCase
import com.picassos.betamax.android.domain.usecase.signout.SignoutUseCase
import com.picassos.betamax.android.domain.usecase.subscription.GetLocalSubscriptionUseCase
import com.picassos.betamax.android.domain.usecase.subscription.SetLocalSubscriptionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun getAccountUseCase(): GetLocalAccountUseCase
    fun setAccountUseCase(): SetLocalAccountUseCase
    fun getConfigurationUseCase(): GetLocalConfigurationUseCase
    fun setConfigurationUseCase(): SetLocalConfigurationUseCase
    fun getSubscriptionUseCase(): GetLocalSubscriptionUseCase
    fun setSubscriptionUseCase(): SetLocalSubscriptionUseCase
}
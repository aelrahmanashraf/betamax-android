package com.picassos.betamax.android.di

import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.data.source.remote.PayPalService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideApi(): APIService {
        return APIService.create()
    }

    @Provides
    @Singleton
    fun providePayPalApi(): PayPalService {
        return PayPalService.create()
    }
}
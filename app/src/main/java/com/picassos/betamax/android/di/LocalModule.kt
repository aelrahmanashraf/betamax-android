package com.picassos.betamax.android.di

import android.content.Context
import com.picassos.betamax.android.data.source.local.datastore.SharedDataManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideSharedDataManager(@ApplicationContext context: Context): SharedDataManager {
        return SharedDataManager(context)
    }
}
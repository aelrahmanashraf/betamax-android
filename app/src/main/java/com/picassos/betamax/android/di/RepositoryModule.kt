package com.picassos.betamax.android.di

import com.picassos.betamax.android.data.repository.*
import com.picassos.betamax.android.data.source.local.datastore.SharedDataManager
import com.picassos.betamax.android.data.source.remote.APIService
import com.picassos.betamax.android.data.source.remote.PayPalService
import com.picassos.betamax.android.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun providerConfigurationRepository(api: APIService, sharedData: SharedDataManager): ConfigurationRepository {
        return ConfigurationRepositoryImpl(api, sharedData)
    }

    @Provides
    @Singleton
    fun providerContinueWatchingRepository(api: APIService): ContinueWatchingRepository {
        return ContinueWatchingRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerPayPalRepository(api: PayPalService): PayPalRepository {
        return PayPalRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerSigninRepository(api: APIService): SigninRepository {
        return SigninRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerSignoutRepository(api: APIService): SignoutRepository {
        return SignoutRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerLaunchRepository(api: APIService): LaunchRepository {
        return LaunchRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerSubscriptionRepository(api: APIService, sharedData: SharedDataManager): SubscriptionRepository {
        return SubscriptionRepositoryImpl(api, sharedData)
    }

    @Provides
    @Singleton
    fun providerRegisterRepository(api: APIService): RegisterRepository {
        return RegisterRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerVerifyCodeRepository(api: APIService): VerifyCodeRepository {
        return VerifyCodeRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerMovieRepository(api: APIService): MovieRepository {
        return MovieRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerTvChannelRepository(api: APIService): TvChannelRepository {
        return TvChannelRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerHomeRepository(api: APIService): HomeRepository {
        return HomeRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerGenreRepository(api: APIService): GenreRepository {
        return GenreRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerResetPasswordRepository(api: APIService): ResetPasswordRepository {
        return ResetPasswordRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerChangePasswordRepository(api: APIService): ChangePasswordRepository {
        return ChangePasswordRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerCastRepository(api: APIService): CastRepository {
        return CastRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun providerEpisodeRepository(api: APIService): SeriesRepository {
        return SeriesRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(api: APIService, sharedData: SharedDataManager): AccountRepository {
        return AccountRepositoryImpl(api, sharedData)
    }

    @Provides
    @Singleton
    fun provideAccountSettingsRepository(api: APIService): AccountSettingsRepository {
        return AccountSettingsRepositoryImpl(api)
    }
}
package com.picassos.betamax.android.presentation.app

import android.app.Application
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.core.utilities.Connectivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi

@HiltAndroidApp
@DelicateCoroutinesApi
class App : Application() {
    companion object {
        @get:Synchronized
        lateinit var instance: App
        lateinit var cache1: androidx.media3.datasource.cache.SimpleCache
        lateinit var cache: SimpleCache

        const val ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID
        const val MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024
    }

    init {
        instance = this
    }

    private val cacheSize: Long = 90 * 1024 * 1024
    private lateinit var cacheEvictor1: androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
    private lateinit var playerDatabaseProvider1: androidx.media3.database.StandaloneDatabaseProvider
    private lateinit var cacheEvictor: LeastRecentlyUsedCacheEvictor
    private lateinit var exoplayerDatabaseProvider: StandaloneDatabaseProvider

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val config = ImagePipelineConfig.newBuilder(this)
            .setMainDiskCacheConfig(
                DiskCacheConfig.newBuilder(this)
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE.toLong())
                    .build())
            .setSmallImageDiskCacheConfig(
                DiskCacheConfig.newBuilder(this)
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE.toLong())
                    .build())
            .setDownsampleEnabled(true)
            .setResizeAndRotateEnabledForNetwork(true)
            .build()
        Fresco.initialize(this, config)

        Connectivity.instance.initializeWithApplicationContext(this)

        FirebaseApp.initializeApp(this)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        exoplayerDatabaseProvider = StandaloneDatabaseProvider(this)
        cache = SimpleCache(cacheDir, cacheEvictor, exoplayerDatabaseProvider)

        cacheEvictor1 = androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor(cacheSize)
        playerDatabaseProvider1 = androidx.media3.database.StandaloneDatabaseProvider(this)
        cache1 = androidx.media3.datasource.cache.SimpleCache(cacheDir, cacheEvictor1, playerDatabaseProvider1)
    }
}
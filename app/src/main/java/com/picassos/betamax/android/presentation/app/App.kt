package com.picassos.betamax.android.presentation.app

import android.app.Application
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
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
        lateinit var playerCache: SimpleCache

        const val ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID
        const val MAX_DISK_CACHE_SIZE = 100 * 1024 * 1024
    }

    init {
        instance = this
    }

    private val cacheSize: Long = 90 * 1024 * 1024
    private lateinit var cacheEvictor: androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
    private lateinit var playerDatabaseProvider: androidx.media3.database.StandaloneDatabaseProvider

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
        playerDatabaseProvider = StandaloneDatabaseProvider(this)
        playerCache = SimpleCache(cacheDir, cacheEvictor, playerDatabaseProvider)
    }
}
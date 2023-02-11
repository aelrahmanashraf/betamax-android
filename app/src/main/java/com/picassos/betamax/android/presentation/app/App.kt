package com.picassos.betamax.android.presentation.app

import android.app.Application
import android.graphics.Bitmap
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.core.utilities.Connectivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi

const val ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID

@HiltAndroidApp
@DelicateCoroutinesApi
class App : Application() {
    companion object {
        @get:Synchronized
        lateinit var instance: App
        lateinit var cache: SimpleCache
    }

    init {
        instance = this
    }

    private val cacheSize: Long = 90 * 1024 * 1024
    private lateinit var cacheEvictor: LeastRecentlyUsedCacheEvictor
    private lateinit var exoplayerDatabaseProvider: ExoDatabaseProvider

    override fun onCreate() {
        super.onCreate()

        val config = ImagePipelineConfig.newBuilder(this)
            .setBitmapsConfig(Bitmap.Config.ARGB_8888)
            .build()
        Fresco.initialize(this, config)

        Connectivity.instance.initializeWithApplicationContext(this)

        FirebaseApp.initializeApp(this)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        exoplayerDatabaseProvider = ExoDatabaseProvider(this)
        cache = SimpleCache(cacheDir, cacheEvictor, exoplayerDatabaseProvider)
    }
}
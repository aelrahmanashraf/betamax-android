package com.picassos.betamax.android.presentation.app

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
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
    override fun onCreate() {
        super.onCreate()

        instance = this

        val config = ImagePipelineConfig.newBuilder(this)
            .setBitmapsConfig(Bitmap.Config.ARGB_8888)
            .build()
        Fresco.initialize(this, config)

        Connectivity.instance.initializeWithApplicationContext(this)

        FirebaseApp.initializeApp(this)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }

    companion object {
        @get:Synchronized
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: App
            private set
    }

    init {
        instance = this
    }
}
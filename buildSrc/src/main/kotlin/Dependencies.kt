object Libraries {
    val plugins = listOf(
        "com.android.application",
        "kotlin-android",
        "kotlin-kapt",
        "com.google.firebase.crashlytics",
        "dagger.hilt.android.plugin",
        "com.google.gms.google-services"
    )
    val implementations = listOf(
        "androidx.core:core-ktx:${Versions.core}",
        "androidx.appcompat:appcompat:${Versions.appCompat}",
        "com.google.android.material:material:${Versions.material}",
        "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}",
        "androidx.activity:activity-ktx:${Versions.activity}",
        "androidx.fragment:fragment-ktx:${Versions.fragment}",
        "androidx.datastore:datastore-preferences:${Versions.dataStore}",
        "androidx.browser:browser:${Versions.browser}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}",
        "com.google.dagger:hilt-android:${Versions.dagger}",
        "com.squareup.retrofit2:retrofit:${Versions.retrofit}",
        "com.squareup.retrofit2:converter-gson:${Versions.retrofit}",
        "com.facebook.fresco:fresco:${Versions.fresco}",
        "com.squareup.picasso:picasso:${Versions.picasso}",
        "com.airbnb.android:lottie:${Versions.lottie}",
        "org.jetbrains:annotations:${Versions.annotations}",
        "com.google.code.gson:gson:${Versions.gson}",
        "androidx.work:work-runtime-ktx:${Versions.work}",
        "com.onesignal:OneSignal:${Versions.onesignal}",
        "com.google.android.exoplayer:exoplayer:${Versions.exoplayer}",
        "com.google.android.exoplayer:exoplayer-ui:${Versions.exoplayer}",
        "androidx.media3:media3-exoplayer:${Versions.media3}",
        "androidx.media3:media3-ui:${Versions.media3}",
        "androidx.media3:media3-exoplayer-hls:${Versions.media3}",
        "me.relex:circleindicator:${Versions.circleIndicator}",
        "com.github.androidmads:QRGenerator:${Versions.qrGenerator}",
        "com.google.firebase:firebase-analytics-ktx",
        "com.google.firebase:firebase-crashlytics-ktx",
        "com.google.firebase:firebase-messaging",
    )
    val kapts = listOf(
        "com.google.dagger:hilt-android-compiler:${Versions.dagger}",
        "androidx.hilt:hilt-compiler:${Versions.hiltCompiler}"
    )
    val testImplementations = listOf(
        "junit:junit:${Versions.junit}"
    )
    val androidTestImplementations = listOf(
        "androidx.test.ext:junit:${Versions.extJunit}",
        "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    )
}
package com.picassos.betamax.android.presentation.app.video_quality

data class VideoQualityState(
    val isLoading: Boolean = false,
    val response: Int? = null,
    val error: String? = null)
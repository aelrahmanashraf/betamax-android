package com.picassos.betamax.android.presentation.app.quality.update_video_quality

data class UpdateVideoQualityState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
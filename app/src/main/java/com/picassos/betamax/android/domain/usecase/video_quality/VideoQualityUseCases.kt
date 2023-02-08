package com.picassos.betamax.android.domain.usecase.video_quality

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class VideoQualityUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getVideoQualityUseCase: GetVideoQualityUseCase,
    val updateVideoQualityUseCase: UpdateVideoQualityUseCase
)
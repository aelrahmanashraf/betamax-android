package com.picassos.betamax.android.domain.usecase.video_quality

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import javax.inject.Inject

data class VideoQualityChooserUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val updateVideoQualityUseCase: UpdateVideoQualityUseCase)
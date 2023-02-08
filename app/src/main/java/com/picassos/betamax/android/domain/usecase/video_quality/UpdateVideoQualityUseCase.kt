package com.picassos.betamax.android.domain.usecase.video_quality

import com.picassos.betamax.android.domain.repository.AccountSettingsRepository
import javax.inject.Inject

class UpdateVideoQualityUseCase @Inject constructor(private val repository: AccountSettingsRepository) {
    suspend operator fun invoke(token: String, quality: Int) =
        repository.updateVideoQuality(token, quality)
}
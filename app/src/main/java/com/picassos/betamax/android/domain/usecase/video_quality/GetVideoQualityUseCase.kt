package com.picassos.betamax.android.domain.usecase.video_quality

import com.picassos.betamax.android.domain.repository.AccountSettingsRepository
import javax.inject.Inject

class GetVideoQualityUseCase @Inject constructor(private val repository: AccountSettingsRepository) {
    suspend operator fun invoke(token: String) =
        repository.getVideoQuality(token)
}
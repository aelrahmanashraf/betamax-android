package com.picassos.betamax.android.domain.usecase.continue_watching

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.ContinueWatchingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateContinueWatchingUseCase @Inject constructor(private val repository: ContinueWatchingRepository) {
    suspend operator fun invoke(token: String, contentId: Int, url: String, thumbnail: String, currentPosition: Int): Flow<Resource<Int>> =
        repository.updateContinueWatching(token, contentId, url, thumbnail, currentPosition)
}
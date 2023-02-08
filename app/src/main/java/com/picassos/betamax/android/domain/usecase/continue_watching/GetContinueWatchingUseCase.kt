package com.picassos.betamax.android.domain.usecase.continue_watching

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.ContinueWatching
import com.picassos.betamax.android.domain.repository.ContinueWatchingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContinueWatchingUseCase @Inject constructor(private val repository: ContinueWatchingRepository) {
    suspend operator fun invoke(token: String): Flow<Resource<ContinueWatching>> =
        repository.getContinueWatching(token)
}
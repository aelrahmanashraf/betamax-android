package com.picassos.betamax.android.domain.usecase.launch

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Launch
import com.picassos.betamax.android.domain.repository.LaunchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LaunchUseCase @Inject constructor(private val repository: LaunchRepository) {
    suspend operator fun invoke(token: String): Flow<Resource<Launch>> =
        repository.launch(token)
}
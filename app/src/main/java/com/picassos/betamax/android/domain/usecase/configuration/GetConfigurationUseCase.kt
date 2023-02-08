package com.picassos.betamax.android.domain.usecase.configuration

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Configuration
import com.picassos.betamax.android.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConfigurationUseCase @Inject constructor(private val repository: ConfigurationRepository) {
    suspend operator fun invoke(): Flow<Resource<Configuration>> =
        repository.getConfiguration()
}
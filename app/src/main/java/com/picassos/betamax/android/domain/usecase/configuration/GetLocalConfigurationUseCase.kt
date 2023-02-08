package com.picassos.betamax.android.domain.usecase.configuration

import com.picassos.betamax.android.domain.model.Configuration
import com.picassos.betamax.android.domain.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class GetLocalConfigurationUseCase @Inject constructor(private val repository: ConfigurationRepository) {
    operator fun invoke(): Flow<Configuration> =
        repository.getLocalConfiguration()
}
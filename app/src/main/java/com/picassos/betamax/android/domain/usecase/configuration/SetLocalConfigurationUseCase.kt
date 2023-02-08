package com.picassos.betamax.android.domain.usecase.configuration

import com.picassos.betamax.android.domain.repository.ConfigurationRepository
import javax.inject.Inject

class SetLocalConfigurationUseCase @Inject constructor(private val repository: ConfigurationRepository) {
    suspend operator fun invoke(configuration: String) =
        repository.setLocalConfiguration(configuration)
}
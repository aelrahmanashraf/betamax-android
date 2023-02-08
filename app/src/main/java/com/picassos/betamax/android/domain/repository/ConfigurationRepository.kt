package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Configuration
import kotlinx.coroutines.flow.Flow

interface ConfigurationRepository {
    fun getLocalConfiguration(): Flow<Configuration>
    suspend fun getConfiguration(): Flow<Resource<Configuration>>
    suspend fun setLocalConfiguration(configuration: String)
}
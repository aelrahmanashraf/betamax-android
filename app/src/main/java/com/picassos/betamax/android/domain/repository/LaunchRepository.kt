package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Launch
import kotlinx.coroutines.flow.Flow

interface LaunchRepository {
    suspend fun launch(token: String): Flow<Resource<Launch>>
}
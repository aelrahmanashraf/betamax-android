package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Cast
import kotlinx.coroutines.flow.Flow

interface CastRepository {
    suspend fun getCast(movieId: Int): Flow<Resource<Cast>>
}
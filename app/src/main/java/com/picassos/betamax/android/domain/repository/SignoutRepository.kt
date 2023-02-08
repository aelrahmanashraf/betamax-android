package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import kotlinx.coroutines.flow.Flow

interface SignoutRepository {
    suspend fun signout(token: String): Flow<Resource<Int>>
}
package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Home
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun getHome(token: String): Flow<Resource<Home>>
}
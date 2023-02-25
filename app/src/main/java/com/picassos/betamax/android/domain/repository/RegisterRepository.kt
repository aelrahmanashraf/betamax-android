package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface RegisterRepository {
    suspend fun register(imei: String, username: String, email: String, password: String): Flow<Resource<Account>>
}
package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface SigninRepository {
    suspend fun signin(email: String, password: String): Flow<Resource<Account>>
}
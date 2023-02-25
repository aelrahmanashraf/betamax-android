package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getLocalAccount(): Flow<Account>
    suspend fun getAccount(token: String, imei: String): Flow<Resource<Account>>
    suspend fun setLocalAccount(account: String)
}
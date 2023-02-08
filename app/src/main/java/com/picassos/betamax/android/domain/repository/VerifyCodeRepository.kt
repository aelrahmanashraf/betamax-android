package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface VerifyCodeRepository {
    suspend fun verifyCode(request: String, email: String, verificationCode: Int): Flow<Resource<Account>>
}
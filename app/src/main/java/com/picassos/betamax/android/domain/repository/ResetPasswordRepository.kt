package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import kotlinx.coroutines.flow.Flow

interface ResetPasswordRepository {
    suspend fun sendResetPasswordEmail(email: String): Flow<Resource<Int>>
    suspend fun resetPassword(token: String, email: String, password: String, confirmPassword: String): Flow<Resource<Int>>
}
package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import kotlinx.coroutines.flow.Flow

interface ChangePasswordRepository {
    suspend fun changePassword(token: String, currentPassword: String, newPassword: String): Flow<Resource<Int>>
}
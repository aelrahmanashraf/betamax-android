package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import kotlinx.coroutines.flow.Flow

interface AccountSettingsRepository {
    suspend fun updateProfileInfo(token: String, username: String): Flow<Resource<Int>>
    suspend fun getVideoQuality(token: String): Flow<Resource<Int>>
    suspend fun updateVideoQuality(token: String, quality: Int): Flow<Resource<Int>>
}
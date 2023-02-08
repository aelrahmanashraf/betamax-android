package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.ContinueWatching
import kotlinx.coroutines.flow.Flow

interface ContinueWatchingRepository {
    suspend fun getContinueWatching(token: String): Flow<Resource<ContinueWatching>>
    suspend fun updateContinueWatching(token: String, contentId: Int, url: String, thumbnail: String, currentPosition: Int): Flow<Resource<Int>>
    suspend fun deleteContinueWatching(token: String, contentId: Int): Flow<Resource<Int>>
}
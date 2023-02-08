package com.picassos.betamax.android.domain.usecase.tvchannel

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.TvChannels
import com.picassos.betamax.android.domain.repository.TvChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedTvChannelsUseCase @Inject constructor(private val repository: TvChannelRepository) {
    suspend operator fun invoke(token: String): Flow<Resource<TvChannels>> =
        repository.getSavedTvChannels(token)
}
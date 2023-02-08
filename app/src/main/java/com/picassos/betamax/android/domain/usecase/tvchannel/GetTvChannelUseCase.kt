package com.picassos.betamax.android.domain.usecase.tvchannel

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.ViewTvChannel
import com.picassos.betamax.android.domain.repository.TvChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTvChannelUseCase @Inject constructor(private val repository: TvChannelRepository) {
    suspend operator fun invoke(token: String, tvChannelId: Int): Flow<Resource<ViewTvChannel>> =
        repository.getTvChannel(token, tvChannelId)
}
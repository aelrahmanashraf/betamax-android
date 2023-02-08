package com.picassos.betamax.android.domain.usecase.tvchannel

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.TvChannelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveTvChannelUseCase @Inject constructor(private val repository: TvChannelRepository) {
    suspend operator fun invoke(token: String, tvChannelId: Int): Flow<Resource<String>> =
        repository.saveTvChannel(token, tvChannelId)
}
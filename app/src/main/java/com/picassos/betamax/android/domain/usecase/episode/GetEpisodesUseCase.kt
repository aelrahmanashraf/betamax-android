package com.picassos.betamax.android.domain.usecase.episode

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Episodes
import com.picassos.betamax.android.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEpisodesUseCase @Inject constructor(private val repository: SeriesRepository) {
    suspend operator fun invoke(token: String, movieId: Int, seasonId: Int): Flow<Resource<Episodes>> =
        repository.getEpisodes(token, movieId, seasonId)
}
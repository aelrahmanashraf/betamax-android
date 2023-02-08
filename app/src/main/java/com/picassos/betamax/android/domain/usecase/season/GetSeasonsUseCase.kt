package com.picassos.betamax.android.domain.usecase.season

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Seasons
import com.picassos.betamax.android.domain.repository.SeriesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSeasonsUseCase @Inject constructor(private val repository: SeriesRepository) {
    suspend operator fun invoke(movieId: Int): Flow<Resource<Seasons>> =
        repository.getSeasons(movieId)
}
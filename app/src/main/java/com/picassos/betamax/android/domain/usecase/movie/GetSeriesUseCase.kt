package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSeriesUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(): Flow<Resource<Movies>> =
        repository.getSeries()
}
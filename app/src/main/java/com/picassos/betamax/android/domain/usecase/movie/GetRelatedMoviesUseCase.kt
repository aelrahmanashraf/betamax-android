package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRelatedMoviesUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(token: String, movieId: Int): Flow<Resource<Movies>> =
        repository.getRelatedMovies(token, movieId)
}
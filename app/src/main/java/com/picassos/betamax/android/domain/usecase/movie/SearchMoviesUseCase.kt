package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMoviesUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(query: String, filter: String): Flow<Resource<Movies>> =
        repository.searchMovies(query, filter)
}
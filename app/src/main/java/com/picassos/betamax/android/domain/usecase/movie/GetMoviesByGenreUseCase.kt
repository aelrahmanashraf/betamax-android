package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Movies
import com.picassos.betamax.android.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesByGenreUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(genreId: Int, filter: String): Flow<Resource<Movies>> =
        repository.getMoviesByGenre(genreId, filter)
}
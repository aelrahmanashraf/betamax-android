package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.ViewMovie
import com.picassos.betamax.android.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(token: String, movieId: Int, seasonLevel: Int, genreId: Int): Flow<Resource<ViewMovie>> =
        repository.getMovie(token, movieId, seasonLevel, genreId)
}
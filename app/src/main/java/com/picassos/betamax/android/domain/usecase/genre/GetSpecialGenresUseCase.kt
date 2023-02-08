package com.picassos.betamax.android.domain.usecase.genre

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Genres
import com.picassos.betamax.android.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpecialGenresUseCase @Inject constructor(private val repository: GenreRepository) {
    suspend operator fun invoke(): Flow<Resource<Genres>> =
        repository.getSpecialGenres()
}
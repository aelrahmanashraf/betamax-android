package com.picassos.betamax.android.domain.usecase.cast

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Cast
import com.picassos.betamax.android.domain.repository.CastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCastUseCase @Inject constructor(private val repository: CastRepository) {
    suspend operator fun invoke(movieId: Int): Flow<Resource<Cast>> =
        repository.getCast(movieId)
}
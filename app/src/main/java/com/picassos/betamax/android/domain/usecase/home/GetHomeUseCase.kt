package com.picassos.betamax.android.domain.usecase.home

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Home
import com.picassos.betamax.android.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHomeUseCase @Inject constructor(private val repository: HomeRepository) {
    suspend operator fun invoke(token: String): Flow<Resource<Home>> =
        repository.getHome(token)
}
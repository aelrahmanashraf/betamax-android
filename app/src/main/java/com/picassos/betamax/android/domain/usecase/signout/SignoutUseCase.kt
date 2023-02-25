package com.picassos.betamax.android.domain.usecase.signout

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.SignoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignoutUseCase @Inject constructor(private val repository: SignoutRepository) {
    suspend operator fun invoke(token: String, imei: String): Flow<Resource<Int>> =
        repository.signout(token, imei)
}
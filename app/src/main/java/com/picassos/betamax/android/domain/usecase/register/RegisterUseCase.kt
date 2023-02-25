package com.picassos.betamax.android.domain.usecase.register

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.repository.RegisterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val repository: RegisterRepository) {
    suspend operator fun invoke(imei: String, username: String, email: String, password: String): Flow<Resource<Account>> =
        repository.register(imei, username, email, password)
}
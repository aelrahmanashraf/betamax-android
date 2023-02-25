package com.picassos.betamax.android.domain.usecase.signin

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.repository.SigninRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SigninUseCase @Inject constructor(private val repository: SigninRepository) {
    suspend operator fun invoke(imei: String, email: String, password: String): Flow<Resource<Account>> =
        repository.signin(imei, email, password)
}
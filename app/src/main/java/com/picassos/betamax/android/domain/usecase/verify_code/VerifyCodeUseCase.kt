package com.picassos.betamax.android.domain.usecase.verify_code

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.repository.VerifyCodeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerifyCodeUseCase @Inject constructor(private val repository: VerifyCodeRepository) {
    suspend operator fun invoke(request: String, email: String, verificationCode: Int): Flow<Resource<Account>> =
        repository.verifyCode(request, email, verificationCode)
}
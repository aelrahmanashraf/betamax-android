package com.picassos.betamax.android.domain.usecase.reset_password

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.ResetPasswordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(private val repository: ResetPasswordRepository) {
    suspend operator fun invoke(token: String, email: String, password: String, confirmPassword: String): Flow<Resource<Int>> =
        repository.resetPassword(token, email, password, confirmPassword)
}
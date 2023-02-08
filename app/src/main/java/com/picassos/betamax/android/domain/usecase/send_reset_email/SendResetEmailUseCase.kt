package com.picassos.betamax.android.domain.usecase.send_reset_email

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.ResetPasswordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendResetEmailUseCase @Inject constructor(private val repository: ResetPasswordRepository) {
    suspend operator fun invoke(email: String): Flow<Resource<Int>> =
        repository.sendResetPasswordEmail(email)
}
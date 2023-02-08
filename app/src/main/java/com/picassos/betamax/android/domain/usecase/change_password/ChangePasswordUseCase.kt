package com.picassos.betamax.android.domain.usecase.change_password

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.ChangePasswordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(private val repository: ChangePasswordRepository) {
    suspend operator fun invoke(token: String, currentPassword: String, newPassword: String): Flow<Resource<Int>> =
        repository.changePassword(token, currentPassword, newPassword)
}
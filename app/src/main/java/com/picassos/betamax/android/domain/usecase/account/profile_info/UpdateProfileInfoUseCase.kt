package com.picassos.betamax.android.domain.usecase.account.profile_info

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.repository.AccountSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProfileInfoUseCase @Inject constructor(private val repository: AccountSettingsRepository) {
    suspend operator fun invoke(token: String, username: String): Flow<Resource<Int>> =
        repository.updateProfileInfo(token, username)
}
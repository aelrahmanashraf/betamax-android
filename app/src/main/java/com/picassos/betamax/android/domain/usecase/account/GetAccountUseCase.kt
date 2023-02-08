package com.picassos.betamax.android.domain.usecase.account

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(private val repository: AccountRepository) {
    suspend operator fun invoke(token: String): Flow<Resource<Account>> =
        repository.getAccount(token)
}
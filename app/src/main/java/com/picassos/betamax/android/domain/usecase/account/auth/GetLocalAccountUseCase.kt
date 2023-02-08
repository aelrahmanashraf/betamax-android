package com.picassos.betamax.android.domain.usecase.account.auth

import com.picassos.betamax.android.domain.model.Account
import com.picassos.betamax.android.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject

class GetLocalAccountUseCase @Inject constructor(private val repository: AccountRepository) {
    operator fun invoke(): Flow<Account> =
        repository.getLocalAccount()
}
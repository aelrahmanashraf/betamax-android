package com.picassos.betamax.android.domain.usecase.account.auth

import com.picassos.betamax.android.domain.repository.AccountRepository
import javax.inject.Inject

class SetLocalAccountUseCase @Inject constructor(private val repository: AccountRepository) {
    suspend operator fun invoke(account: String) =
        repository.setLocalAccount(account)
}
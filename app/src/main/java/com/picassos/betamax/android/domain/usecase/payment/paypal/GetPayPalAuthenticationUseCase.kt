package com.picassos.betamax.android.domain.usecase.payment.paypal

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.paypal.PayPalAuthentication
import com.picassos.betamax.android.domain.repository.PayPalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPayPalAuthenticationUseCase @Inject constructor(private val repository: PayPalRepository) {
    suspend operator fun invoke(): Flow<Resource<PayPalAuthentication>> =
        repository.getAuthentication()
}
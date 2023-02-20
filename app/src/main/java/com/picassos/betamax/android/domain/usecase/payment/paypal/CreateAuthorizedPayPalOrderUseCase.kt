package com.picassos.betamax.android.domain.usecase.payment.paypal

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.domain.model.paypal.PayPalCreateAuthorizedOrder
import com.picassos.betamax.android.domain.repository.PayPalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateAuthorizedPayPalOrderUseCase @Inject constructor(private val repository: PayPalRepository) {
    suspend operator fun invoke(requestId: String, order: PayPalCreateOrderBody): Flow<Resource<PayPalCreateAuthorizedOrder>> =
        repository.createAuthorizedOrder(requestId, order)
}
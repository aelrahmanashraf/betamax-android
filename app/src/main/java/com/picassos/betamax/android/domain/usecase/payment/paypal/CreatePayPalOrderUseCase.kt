package com.picassos.betamax.android.domain.usecase.payment.paypal

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.domain.model.paypal.PayPalCreateOrder
import com.picassos.betamax.android.domain.repository.PayPalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreatePayPalOrderUseCase @Inject constructor(private val repository: PayPalRepository) {
    suspend operator fun invoke(authentication: String, requestId: String, order: PayPalCreateOrderBody): Flow<Resource<PayPalCreateOrder>> =
        repository.createOrder(authentication, requestId, order)
}
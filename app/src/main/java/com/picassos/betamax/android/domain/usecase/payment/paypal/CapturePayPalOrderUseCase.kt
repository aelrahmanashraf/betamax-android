package com.picassos.betamax.android.domain.usecase.payment.paypal

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.paypal.PayPalCaptureOrder
import com.picassos.betamax.android.domain.repository.PayPalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CapturePayPalOrderUseCase @Inject constructor(private val repository: PayPalRepository) {
    suspend operator fun invoke(authorization: String, requestId: String, orderId: String): Flow<Resource<PayPalCaptureOrder>> =
        repository.captureOrder(authorization, requestId, orderId)
}
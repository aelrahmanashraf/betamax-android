package com.picassos.betamax.android.domain.usecase.payment.paypal

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.model.paypal.PayPalCaptureAuthorizedOrder
import com.picassos.betamax.android.domain.repository.PayPalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CaptureAuthorizedPayPalOrderUseCase @Inject constructor(private val repository: PayPalRepository) {
    suspend operator fun invoke(requestId: String, orderId: String): Flow<Resource<PayPalCaptureAuthorizedOrder>> =
        repository.captureAuthorizedOrder(requestId, orderId)
}
package com.picassos.betamax.android.domain.usecase.payment.paypal

import javax.inject.Inject

class PayPalUseCases @Inject constructor(
    val getPayPalAuthenticationUseCase: GetPayPalAuthenticationUseCase,
    val createPayPalOrderUseCase: CreatePayPalOrderUseCase,
    val createAuthorizedPayPalOrderUseCase: CreateAuthorizedPayPalOrderUseCase,
    val capturePayPalOrderUseCase: CapturePayPalOrderUseCase,
    val captureAuthorizedPayPalOrderUseCase: CaptureAuthorizedPayPalOrderUseCase)
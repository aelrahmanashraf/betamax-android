package com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_capture_authorized_order

import com.picassos.betamax.android.domain.model.paypal.PayPalCaptureAuthorizedOrder

data class PayPalCaptureAuthorizedOrderState(
    val isLoading: Boolean = false,
    val response: PayPalCaptureAuthorizedOrder? = null,
    val error: String? = null)

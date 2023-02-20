package com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_capture_order

import com.picassos.betamax.android.domain.model.paypal.PayPalCaptureOrder

data class PayPalCaptureOrderState(
    val isLoading: Boolean = false,
    val response: PayPalCaptureOrder? = null,
    val error: String? = null)
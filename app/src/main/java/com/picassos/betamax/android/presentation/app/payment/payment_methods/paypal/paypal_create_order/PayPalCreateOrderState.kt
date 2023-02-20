package com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_create_order

import com.picassos.betamax.android.domain.model.paypal.PayPalCreateOrder

data class PayPalCreateOrderState(
    val isLoading: Boolean = false,
    val response: PayPalCreateOrder? = null,
    val error: String? = null)

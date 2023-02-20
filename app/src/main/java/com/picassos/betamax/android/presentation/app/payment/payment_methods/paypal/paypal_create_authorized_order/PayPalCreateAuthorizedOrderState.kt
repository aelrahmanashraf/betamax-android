package com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_create_authorized_order

import com.picassos.betamax.android.domain.model.paypal.PayPalCreateAuthorizedOrder

data class PayPalCreateAuthorizedOrderState(
    val isLoading: Boolean = false,
    val response: PayPalCreateAuthorizedOrder? = null,
    val error: String? = null)

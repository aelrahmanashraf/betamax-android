package com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_authentication

import com.picassos.betamax.android.domain.model.paypal.PayPalAuthentication

data class PayPalAuthenticationState(
    val isLoading: Boolean = false,
    val response: PayPalAuthentication? = null,
    val error: String? = null)
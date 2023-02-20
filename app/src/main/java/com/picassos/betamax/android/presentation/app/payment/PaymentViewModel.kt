package com.picassos.betamax.android.presentation.app.payment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.picassos.betamax.android.domain.model.SubscriptionPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(app: Application): AndroidViewModel(app) {
    private val _subscriptionPackage = MutableStateFlow(SubscriptionPackage())
    val subscriptionPackage = _subscriptionPackage.asStateFlow()

    fun setSubscriptionPackage(subscriptionPackage: SubscriptionPackage) {
        _subscriptionPackage.tryEmit(subscriptionPackage)
    }

    private val _selectedPaymentMethod = MutableStateFlow<String?>(null)
    val selectedPaymentMethod = _selectedPaymentMethod.asStateFlow()

    fun setSelectedPaymentMethod(paymentMethod: String) {
        _selectedPaymentMethod.tryEmit(paymentMethod)
    }
}
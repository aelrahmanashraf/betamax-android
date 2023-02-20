package com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.domain.usecase.payment.paypal.PayPalUseCases
import com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_authentication.PayPalAuthenticationState
import com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_capture_authorized_order.PayPalCaptureAuthorizedOrderState
import com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_capture_order.PayPalCaptureOrderState
import com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_create_authorized_order.PayPalCreateAuthorizedOrderState
import com.picassos.betamax.android.presentation.app.payment.payment_methods.paypal.paypal_create_order.PayPalCreateOrderState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayPalViewModel @Inject constructor(app: Application, private val payPalUseCases: PayPalUseCases): AndroidViewModel(app) {
    private val _requestId = MutableStateFlow<String?>(null)
    val requestId = _requestId.asStateFlow()

    fun setRequestId(requestId: String) {
        _requestId.tryEmit(requestId)
    }

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken = _accessToken.asStateFlow()

    fun setAccessToken(accessToken: String) {
        _accessToken.tryEmit(accessToken)
    }

    private val _authentication = MutableStateFlow(PayPalAuthenticationState())
    val authentication = _authentication.asStateFlow()

    fun requestAuthentication() {
        viewModelScope.launch {
            payPalUseCases.getPayPalAuthenticationUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _authentication.emit(PayPalAuthenticationState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _authentication.emit(PayPalAuthenticationState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _authentication.emit(PayPalAuthenticationState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _createOrder = MutableStateFlow(PayPalCreateOrderState())
    val createOrder = _createOrder.asStateFlow()

    fun requestCreateOrder(authentication: String, requestId: String, order: PayPalCreateOrderBody) {
        viewModelScope.launch {
            payPalUseCases.createPayPalOrderUseCase(authentication, requestId, order).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _createOrder.emit(PayPalCreateOrderState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _createOrder.emit(PayPalCreateOrderState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _createOrder.emit(PayPalCreateOrderState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _createAuthorizedOrder = MutableStateFlow(PayPalCreateAuthorizedOrderState())
    val createAuthorizedOrder = _createAuthorizedOrder.asStateFlow()

    fun requestCreateAuthorizedOrder(requestId: String, order: PayPalCreateOrderBody) {
        viewModelScope.launch {
            payPalUseCases.createAuthorizedPayPalOrderUseCase(requestId, order).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _createAuthorizedOrder.emit(PayPalCreateAuthorizedOrderState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _createAuthorizedOrder.emit(PayPalCreateAuthorizedOrderState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _createAuthorizedOrder.emit(PayPalCreateAuthorizedOrderState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _captureOrder = MutableStateFlow(PayPalCaptureOrderState())
    val captureOrder = _captureOrder.asStateFlow()

    fun requestCaptureOrder(authentication: String, requestId: String, orderId: String) {
        viewModelScope.launch {
            payPalUseCases.capturePayPalOrderUseCase(authentication, requestId, orderId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _captureOrder.emit(PayPalCaptureOrderState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _captureOrder.emit(PayPalCaptureOrderState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _captureOrder.emit(PayPalCaptureOrderState(
                            error = result.message))
                    }
                }
            }
        }
    }

    private val _captureAuthorizedOrder = MutableStateFlow(PayPalCaptureAuthorizedOrderState())
    val captureAuthorizedOrder = _captureAuthorizedOrder.asStateFlow()

    fun requestCaptureAuthorizedOrder(requestId: String, orderId: String) {
        viewModelScope.launch {
            payPalUseCases.captureAuthorizedPayPalOrderUseCase(requestId, orderId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _captureAuthorizedOrder.emit(PayPalCaptureAuthorizedOrderState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _captureAuthorizedOrder.emit(PayPalCaptureAuthorizedOrderState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _captureAuthorizedOrder.emit(PayPalCaptureAuthorizedOrderState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
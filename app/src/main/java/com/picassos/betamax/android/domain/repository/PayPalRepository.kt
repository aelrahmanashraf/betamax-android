package com.picassos.betamax.android.domain.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.domain.model.paypal.*
import kotlinx.coroutines.flow.Flow

interface PayPalRepository {
    suspend fun getAuthentication(): Flow<Resource<PayPalAuthentication>>
    suspend fun createOrder(authorization: String, requestId: String, order: PayPalCreateOrderBody): Flow<Resource<PayPalCreateOrder>>
    suspend fun createAuthorizedOrder(requestId: String, order: PayPalCreateOrderBody): Flow<Resource<PayPalCreateAuthorizedOrder>>
    suspend fun captureOrder(authorization: String, requestId: String, orderId: String): Flow<Resource<PayPalCaptureOrder>>
    suspend fun captureAuthorizedOrder(requestId: String, orderId: String): Flow<Resource<PayPalCaptureAuthorizedOrder>>
}
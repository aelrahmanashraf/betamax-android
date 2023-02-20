package com.picassos.betamax.android.data.repository

import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.core.utilities.Response
import com.picassos.betamax.android.data.mapper.paypal.toPayPalAuthentication
import com.picassos.betamax.android.data.mapper.paypal.toPayPalCaptureOrder
import com.picassos.betamax.android.data.mapper.paypal.toPayPalCreateOrder
import com.picassos.betamax.android.data.source.remote.PayPalService
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.domain.model.paypal.*
import com.picassos.betamax.android.domain.repository.PayPalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayPalRepositoryImpl @Inject constructor(private val service: PayPalService): PayPalRepository {
    override suspend fun getAuthentication(): Flow<Resource<PayPalAuthentication>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.authentication()
                emit(Resource.Success(response.toPayPalAuthentication()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun createOrder(authorization: String, requestId: String, order: PayPalCreateOrderBody): Flow<Resource<PayPalCreateOrder>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.createOrder(
                    authorization = authorization,
                    requestId = requestId,
                    order = order)
                emit(Resource.Success(response.toPayPalCreateOrder()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun createAuthorizedOrder(requestId: String, order: PayPalCreateOrderBody): Flow<Resource<PayPalCreateAuthorizedOrder>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                coroutineScope {
                    val authentication = async(Dispatchers.IO) {
                        service.authentication()
                    }
                    val createOrder = async(Dispatchers.IO) {
                        service.createOrder(
                            authorization = "${authentication.await().tokenType} ${authentication.await().accessToken}",
                            requestId = requestId,
                            order = order)
                    }
                    emit(Resource.Success(
                        PayPalCreateAuthorizedOrder(
                        authentication = authentication.await().toPayPalAuthentication(),
                        order = createOrder.await().toPayPalCreateOrder())
                    ))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun captureOrder(authorization: String, requestId: String, orderId: String): Flow<Resource<PayPalCaptureOrder>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = service.captureOrder(
                    authorization = authorization,
                    requestId = requestId,
                    orderId = orderId)
                emit(Resource.Success(response.toPayPalCaptureOrder()))
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun captureAuthorizedOrder(requestId: String, orderId: String): Flow<Resource<PayPalCaptureAuthorizedOrder>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                coroutineScope {
                    val authentication = async(Dispatchers.IO) {
                        service.authentication()
                    }
                    val captureOrder = async(Dispatchers.IO) {
                        service.captureOrder(
                            authorization = "${authentication.await().tokenType} ${authentication.await().accessToken}",
                            requestId = requestId,
                            orderId = orderId)
                    }
                    emit(Resource.Success(
                        PayPalCaptureAuthorizedOrder(
                        authentication = authentication.await().toPayPalAuthentication(),
                        captureOrder = captureOrder.await().toPayPalCaptureOrder())
                    ))
                }
            } catch (t: Throwable) {
                when (t) {
                    is IOException -> emit(Resource.Error(Response.NETWORK_FAILURE_EXCEPTION))
                    else -> emit(Resource.Error(Response.MALFORMED_REQUEST_EXCEPTION))
                }
            }
            emit(Resource.Loading(false))
        }
    }
}
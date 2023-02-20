package com.picassos.betamax.android.data.source.remote

import com.picassos.betamax.android.BuildConfig
import com.picassos.betamax.android.core.configuration.Config
import com.picassos.betamax.android.core.utilities.ConnectivityInterceptor
import com.picassos.betamax.android.core.utilities.Security
import com.picassos.betamax.android.data.source.remote.body.paypal.PayPalCreateOrderBody
import com.picassos.betamax.android.data.source.remote.dto.paypal.PayPalAuthenticationDto
import com.picassos.betamax.android.data.source.remote.dto.paypal.PayPalCaptureOrderDto
import com.picassos.betamax.android.data.source.remote.dto.paypal.PayPalCreateOrderDto
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface PayPalService {
    @FormUrlEncoded
    @Headers(
        "Accept: application/json",
        "Content-Type: application/x-www-form-urlencoded")
    @POST("v1/oauth2/token")
    suspend fun authentication(
        @Header("Authorization") authorization: String =
            "Basic " + Security.encodeStringToBase64("${CLIENT_ID}:${SECRET_KEY}"),
        @Field("grant_type") grantType: String = "client_credentials"
    ): PayPalAuthenticationDto

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("v2/checkout/orders")
    suspend fun createOrder(
        @Header("Authorization") authorization: String,
        @Header("PayPal-Request-Id") requestId: String,
        @Body order: PayPalCreateOrderBody
    ): PayPalCreateOrderDto

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json")
    @POST("v2/checkout/orders/{order_id}/capture")
    suspend fun captureOrder(
        @Header("Authorization") authorization: String,
        @Header("PayPal-Request-Id") requestId: String,
        @Path("order_id") orderId: String
    ): PayPalCaptureOrderDto

    companion object {
        private val BASE_URL = when (Config.BUILD_TYPEcls) {
            "release" -> "https://api-m.paypal.com"
            else -> "https://api-m.sandbox.paypal.com/"
        }
        const val CLIENT_ID = BuildConfig.PAYPAL_CLIENT_ID
        const val SECRET_KEY = BuildConfig.PAYPAL_SECRET_KEY
        const val APP_PREFIX = "app://betamax"

        private val okHttpClient by lazy {
            OkHttpClient.Builder()
                .addInterceptor(ConnectivityInterceptor())
                .build()
        }

        fun create(): PayPalService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PayPalService::class.java)
        }
    }
}
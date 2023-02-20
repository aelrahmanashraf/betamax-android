package com.picassos.betamax.android.data.source.remote.body.paypal

import com.google.gson.annotations.SerializedName

data class PayPalCreateOrderBody(
    @SerializedName("intent")
    val intent: String,
    @SerializedName("purchase_units")
    val purchaseUnits: List<PurchaseUnit>,
    @SerializedName("payment_source")
    val paymentSource: PaymentSource) {

    data class PurchaseUnit(
        @SerializedName("amount")
        val amount: Amount) {

        data class Amount(
            @SerializedName("currency_code")
            val currencyCode: String,
            @SerializedName("value")
            val value: String)
    }

    data class PaymentSource(
        @SerializedName("paypal")
        val paypal: PayPal) {

        data class PayPal(
            @SerializedName("experience_context")
            val experienceContext: ExperienceContext) {

            data class ExperienceContext(
                @SerializedName("brand_name")
                val brandName: String,
                @SerializedName("locale")
                val locale: String,
                @SerializedName("landing_page")
                val landingPage: String,
                @SerializedName("user_action")
                val userAction: String,
                @SerializedName("return_url")
                val returnUrl: String,
                @SerializedName("cancel_url")
                val cancelUrl: String)
        }
    }
}
package com.picassos.betamax.android.data.source.remote.dto.paypal

import com.google.gson.annotations.SerializedName

data class PayPalCaptureOrderDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("payment_source")
    val paymentSource: PaymentSource,
    @SerializedName("purchase_units")
    val purchaseUnits: List<PurchaseUnit>) {

    data class PaymentSource(
        @SerializedName("paypal")
        val paypal: PayPal) {

        data class PayPal(
            @SerializedName("name")
            val name: Name,
            @SerializedName("email_address")
            val emailAddress: String,
            @SerializedName("account_id")
            val accountId: String) {

            data class Name(
                @SerializedName("given_name")
                val givenName: String,
                @SerializedName("surname")
                val surname: String)
        }
    }

    data class PurchaseUnit(
        @SerializedName("reference_id")
        val referenceId: String,
        @SerializedName("payments")
        val payments: Payments) {

        data class Payments(
            @SerializedName("captures")
            val captures: List<Capture>) {

            data class Capture(
                @SerializedName("id")
                val id: String,
                @SerializedName("status")
                val status: String,
                @SerializedName("amount")
                val amount: Amount) {

                data class Amount(
                    @SerializedName("currency_code")
                    val currencyCode: String,
                    @SerializedName("value")
                    val value: String)
            }
        }
    }
}
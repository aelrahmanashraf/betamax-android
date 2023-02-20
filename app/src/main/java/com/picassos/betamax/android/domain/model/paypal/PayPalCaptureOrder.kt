package com.picassos.betamax.android.domain.model.paypal

data class PayPalCaptureOrder(
    val id: String,
    val status: String,
    val paymentSource: PaymentSource,
    val purchaseUnits: List<PurchaseUnit>) {

    data class PaymentSource(
        val paypal: PayPal) {

        data class PayPal(
            val name: Name,
            val emailAddress: String,
            val accountId: String) {

            data class Name(
                val givenName: String,
                val surname: String)
        }
    }

    data class PurchaseUnit(
        val referenceId: String,
        val payments: Payments) {

        data class Payments(
            val captures: List<Capture>) {

            data class Capture(
                val id: String,
                val status: String,
                val amount: Amount) {

                data class Amount(
                    val currencyCode: String,
                    val value: String)
            }
        }
    }
}

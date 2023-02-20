package com.picassos.betamax.android.domain.model.paypal

data class PayPalCreateOrder(
    val id: String,
    val links: List<Link>,
    val status: String) {

    data class Link(
        val href: String,
        val method: String,
        val rel: String)
}

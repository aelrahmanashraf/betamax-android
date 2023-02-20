package com.picassos.betamax.android.data.mapper.paypal

import com.picassos.betamax.android.data.source.remote.dto.paypal.PayPalCaptureOrderDto
import com.picassos.betamax.android.domain.model.paypal.PayPalCaptureOrder

fun PayPalCaptureOrderDto.toPayPalCaptureOrder(): PayPalCaptureOrder {
    return PayPalCaptureOrder(
        id = id,
        status = status,
        paymentSource = PayPalCaptureOrder.PaymentSource(
            paypal = PayPalCaptureOrder.PaymentSource.PayPal(
                name = PayPalCaptureOrder.PaymentSource.PayPal.Name(
                    givenName = paymentSource.paypal.name.givenName,
                    surname = paymentSource.paypal.name.surname
                ),
                emailAddress = paymentSource.paypal.emailAddress,
                accountId = paymentSource.paypal.accountId
            )
        ),
        purchaseUnits = purchaseUnits.map { purchaseUnit ->
            PayPalCaptureOrder.PurchaseUnit(
                referenceId = purchaseUnit.referenceId,
                PayPalCaptureOrder.PurchaseUnit.Payments(
                    captures = purchaseUnit.payments.captures.map { capture ->
                        PayPalCaptureOrder.PurchaseUnit.Payments.Capture(
                            id = capture.id,
                            status = capture.status,
                            amount = PayPalCaptureOrder.PurchaseUnit.Payments.Capture.Amount(
                                currencyCode = capture.amount.currencyCode,
                                value = capture.amount.value
                            )
                        )
                    }
                )
            )
        }
    )
}
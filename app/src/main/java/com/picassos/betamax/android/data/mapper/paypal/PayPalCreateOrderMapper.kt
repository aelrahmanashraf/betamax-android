package com.picassos.betamax.android.data.mapper.paypal

import com.picassos.betamax.android.data.source.remote.dto.paypal.PayPalCreateOrderDto
import com.picassos.betamax.android.domain.model.paypal.PayPalCreateOrder

fun PayPalCreateOrderDto.toPayPalCreateOrder(): PayPalCreateOrder {
    return PayPalCreateOrder(
        id = id,
        status = status,
        links = links.map { link ->
            PayPalCreateOrder.Link(
                href = link.href,
                method = link.method,
                rel = link.rel)
        }
    )
}
package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.AccountDto
import com.picassos.betamax.android.domain.model.Account

fun AccountDto.toAccount(): Account {
    return Account(
        token = account.details.token,
        username = account.details.username,
        emailAddress = account.details.emailAddress,
        emailConfirmed = account.details.emailConfirmed,
        responseCode =  account.responseCode.code)
}
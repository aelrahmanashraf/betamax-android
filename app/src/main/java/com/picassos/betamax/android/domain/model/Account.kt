package com.picassos.betamax.android.domain.model

import com.picassos.betamax.android.core.utilities.Response.CREDENTIALS_NOT_SET
import java.io.Serializable

data class Account(
    val token: String = CREDENTIALS_NOT_SET,
    val paymentToken: String = CREDENTIALS_NOT_SET,
    val username: String = CREDENTIALS_NOT_SET,
    val emailAddress: String = CREDENTIALS_NOT_SET,
    val emailConfirmed: Int = 0,
    val responseCode: Int = 0): Serializable

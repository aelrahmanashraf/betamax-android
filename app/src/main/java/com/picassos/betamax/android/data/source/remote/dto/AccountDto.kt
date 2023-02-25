package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountDto(
    @SerializedName("account")
    val account: Account) {

    data class Account(
        @SerializedName("details")
        val details: Details,
        @SerializedName("response")
        val responseCode: ResponseCode) {

        data class Details(
            @SerializedName("token")
            val token: String,
            @SerializedName("username")
            val username: String,
            @SerializedName("email_address")
            val emailAddress: String = "",
            @SerializedName("email_confirmed")
            val emailConfirmed: Int = 0): Serializable

        data class ResponseCode(
            @SerializedName("code")
            val code: Int)
    }
}

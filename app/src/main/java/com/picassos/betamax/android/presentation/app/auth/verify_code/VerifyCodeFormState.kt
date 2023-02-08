package com.picassos.betamax.android.presentation.app.auth.verify_code

data class VerifyCodeFormState(
    val verificationCode: String = "",
    val verificationCodeError: String? = "")
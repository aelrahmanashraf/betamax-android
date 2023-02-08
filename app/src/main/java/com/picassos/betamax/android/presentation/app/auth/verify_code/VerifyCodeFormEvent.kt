package com.picassos.betamax.android.presentation.app.auth.verify_code

sealed class VerifyCodeFormEvent {
    data class VerificationCodeChanged(val verificationCode: String): VerifyCodeFormEvent()
    object Validate: VerifyCodeFormEvent()
    object Submit: VerifyCodeFormEvent()
}
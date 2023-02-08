package com.picassos.betamax.android.domain.usecase.verify_code

import com.picassos.betamax.android.domain.usecase.form_validation.ValidateVerificationCode
import javax.inject.Inject


data class VerifyCodeValidation @Inject constructor(
    val verificationCodeValidation: ValidateVerificationCode)
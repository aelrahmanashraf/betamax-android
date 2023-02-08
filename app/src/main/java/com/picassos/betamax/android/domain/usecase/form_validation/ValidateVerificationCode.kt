package com.picassos.betamax.android.domain.usecase.form_validation

import com.picassos.betamax.android.core.validation.FormValidation
import javax.inject.Inject

class ValidateVerificationCode @Inject constructor() {
    fun execute(verificationCode: String): ValidationResult {
        if (verificationCode.length != 6) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.VERIFICATION_CODE_INVALID)
        }
        return ValidationResult(
            successful = true)
    }
}
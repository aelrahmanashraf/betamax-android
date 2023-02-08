package com.picassos.betamax.android.domain.usecase.form_validation

import android.util.Patterns
import com.picassos.betamax.android.core.validation.FormValidation
import javax.inject.Inject

class ValidateEmail @Inject constructor() {
    fun execute(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.EMAIL_EMPTY
            )
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.EMAIL_INVALID
            )
        }
        return ValidationResult(
            successful = true
        )
    }
}
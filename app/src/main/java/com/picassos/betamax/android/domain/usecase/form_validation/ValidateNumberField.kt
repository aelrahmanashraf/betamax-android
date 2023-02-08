package com.picassos.betamax.android.domain.usecase.form_validation

import com.picassos.betamax.android.core.validation.FormValidation
import javax.inject.Inject

class ValidateNumberField @Inject constructor() {
    fun execute(value: String): ValidationResult {
        if (value.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.FIELD_EMPTY)
        }
        // TODO: Make sure that there is no any special characters.
        return ValidationResult(
            successful = true
        )
    }
}
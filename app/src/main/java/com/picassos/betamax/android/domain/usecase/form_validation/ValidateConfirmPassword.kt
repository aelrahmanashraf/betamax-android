package com.picassos.betamax.android.domain.usecase.form_validation

import com.picassos.betamax.android.core.validation.FormValidation
import javax.inject.Inject

class ValidateConfirmPassword @Inject constructor() {
    fun execute(password: String, confirmPassword: String): ValidationResult {
        if (confirmPassword.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.CONFIRM_PASSWORD_EMPTY
            )
        }
        if (password != confirmPassword) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.PASSWORDS_NOT_MATCH
            )
        }
        return ValidationResult(
            successful = true
        )
    }
}
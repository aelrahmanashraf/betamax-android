package com.picassos.betamax.android.domain.usecase.form_validation

import com.picassos.betamax.android.core.validation.FormValidation
import javax.inject.Inject

class ValidateUsername @Inject constructor() {
    fun execute(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.USERNAME_EMPTY
            )
        }
        if (!username.matches(USERNAME_REGEX)) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.USERNAME_INVALID
            )
        }
        return ValidationResult(
            successful = true
        )
    }

    companion object {
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9._-]{3,}\$")
    }
}
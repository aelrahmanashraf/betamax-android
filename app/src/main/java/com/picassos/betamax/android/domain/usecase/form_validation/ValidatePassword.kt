package com.picassos.betamax.android.domain.usecase.form_validation

import com.picassos.betamax.android.core.validation.FormValidation
import javax.inject.Inject

class ValidatePassword @Inject constructor() {
    fun execute(password: String): ValidationResult {
        val containsLettersAndDigits = password.any { character -> character.isDigit() }
            && password.any { character -> character.isLetter() }
        if (password.length < 8 || !containsLettersAndDigits) {
            return ValidationResult(
                successful = false,
                errorMessage = FormValidation.PASSWORD_INVALID
            )
        }
        return ValidationResult(
            successful = true
        )
    }
}
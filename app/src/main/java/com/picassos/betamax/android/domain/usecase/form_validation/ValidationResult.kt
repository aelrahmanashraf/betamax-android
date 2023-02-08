package com.picassos.betamax.android.domain.usecase.form_validation

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: String? = null)
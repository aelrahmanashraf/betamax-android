package com.picassos.betamax.android.presentation.app.auth.change_password

data class ChangePasswordFormState(
    val currentPassword: String = "",
    val currentPasswordError: String? = "",
    val newPassword: String = "",
    val newPasswordError: String? = "",
    val confirmPassword: String = "",
    val confirmPasswordError: String? = "")

package com.picassos.betamax.android.presentation.app.auth.profile_info.update_profile_info

data class UpdateProfileInfoState(
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
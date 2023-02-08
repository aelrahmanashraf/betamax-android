package com.picassos.betamax.android.domain.usecase.account.profile_info

import com.picassos.betamax.android.domain.usecase.form_validation.ValidateUsername
import javax.inject.Inject

data class ProfileInfoValidation @Inject constructor(
    val usernameValidation: ValidateUsername)
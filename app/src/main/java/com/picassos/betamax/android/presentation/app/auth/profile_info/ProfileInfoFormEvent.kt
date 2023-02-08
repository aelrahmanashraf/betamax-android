package com.picassos.betamax.android.presentation.app.auth.profile_info

sealed class ProfileInfoFormEvent {
    data class UsernameChanged(val username: String): ProfileInfoFormEvent()
    object Submit: ProfileInfoFormEvent()
}
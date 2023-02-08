package com.picassos.betamax.android.presentation.app.continue_watching.update_continue_watching

data class UpdateContinueWatchingState (
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
package com.picassos.betamax.android.presentation.app.continue_watching.delete_continue_watching

data class DeleteContinueWatchingState (
    val isLoading: Boolean = false,
    val responseCode: Int? = null,
    val error: String? = null)
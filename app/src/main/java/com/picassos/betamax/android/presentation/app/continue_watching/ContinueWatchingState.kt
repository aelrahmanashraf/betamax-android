package com.picassos.betamax.android.presentation.app.continue_watching

import com.picassos.betamax.android.domain.model.ContinueWatching

data class ContinueWatchingState(
    val isLoading: Boolean = false,
    val response: ContinueWatching? = null,
    val error: String? = null)
package com.picassos.betamax.android.presentation.app.season.seasons

import com.picassos.betamax.android.domain.model.Seasons

data class SeasonsState(
    val isLoading: Boolean = false,
    val response: Seasons? = null,
    val error: String? = null)
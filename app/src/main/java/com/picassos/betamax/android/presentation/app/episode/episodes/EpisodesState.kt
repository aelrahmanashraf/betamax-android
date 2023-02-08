package com.picassos.betamax.android.presentation.app.episode.episodes

import com.picassos.betamax.android.domain.model.Episodes

data class EpisodesState(
    val isLoading: Boolean = false,
    val response: Episodes? = null,
    val error: String? = null)
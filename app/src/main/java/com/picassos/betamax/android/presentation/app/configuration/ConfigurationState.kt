package com.picassos.betamax.android.presentation.app.configuration

import com.picassos.betamax.android.domain.model.Configuration

data class ConfigurationState(
    val isLoading: Boolean = false,
    val response: Configuration? = null,
    val error: String? = null)
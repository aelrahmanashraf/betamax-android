package com.picassos.betamax.android.presentation.app.tvchannel.view_tvchannel

import com.picassos.betamax.android.domain.model.ViewTvChannel

data class ViewTvChannelState(
    val isLoading: Boolean = false,
    val response: ViewTvChannel? = null,
    val error: String? = null)
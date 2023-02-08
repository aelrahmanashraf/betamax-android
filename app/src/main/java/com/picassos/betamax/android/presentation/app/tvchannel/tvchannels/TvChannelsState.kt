package com.picassos.betamax.android.presentation.app.tvchannel.tvchannels

import com.picassos.betamax.android.domain.model.TvChannels

data class TvChannelsState(
    val isLoading: Boolean = false,
    val response: TvChannels? = null,
    val error: String? = null)
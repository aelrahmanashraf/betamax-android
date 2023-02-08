package com.picassos.betamax.android.presentation.television.tvchannel.save_tvchannel

data class SaveTvChannelState(
    val isLoading: Boolean = false,
    val responseCode: String? = null,
    val error: String? = null)
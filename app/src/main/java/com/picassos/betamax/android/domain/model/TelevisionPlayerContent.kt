package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class TelevisionPlayerContent(
    val url: String,
    val userAgent: String,
    val currentTvChannelPosition: Int,
    val tvChannelsList: List<TvChannels.TvChannel>): Serializable
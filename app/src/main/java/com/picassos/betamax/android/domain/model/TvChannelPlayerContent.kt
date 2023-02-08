package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class TvChannelPlayerContent(
    val url: String,
    val userAgent: String,
    val currentPosition: Int = 0): Serializable
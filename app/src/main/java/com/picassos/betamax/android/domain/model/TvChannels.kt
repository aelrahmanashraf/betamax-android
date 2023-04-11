package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class TvChannels(
    val tvChannels: List<TvChannel>) {

    data class TvChannel(
        val id: Int,
        val order: Int = 0,
        val tvChannelId: Int,
        val sdUrl: String = "",
        val hdUrl: String = "",
        val fhdUrl: String = "",
        val userAgent: String = "",
        val title: String,
        val banner: String,
        var position: Int = 0): Serializable

    init {
        tvChannels.forEachIndexed { index, tvChannels ->
            tvChannels.position = index
        }
    }
}
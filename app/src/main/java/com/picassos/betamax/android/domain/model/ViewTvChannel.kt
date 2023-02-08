package com.picassos.betamax.android.domain.model

data class ViewTvChannel(
    val tvChannelDetails: TvChannels,
    val tvGenres: Genres,
    val relatedTvChannels: TvChannels,
    val tvChannelSaved: Int,
    val videoQuality: Int)
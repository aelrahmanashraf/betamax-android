package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.TvChannelsDto
import com.picassos.betamax.android.domain.model.TvChannels

fun TvChannelsDto.toTvChannels(): TvChannels {
    return TvChannels(
        tvChannels = tvChannels.map { tvChannel ->
            TvChannels.TvChannel(
                id = tvChannel.id,
                tvChannelId = tvChannel.tvChannelId,
                sdUrl = tvChannel.sdUrl,
                hdUrl = tvChannel.hdUrl,
                fhdUrl = tvChannel.fhdUrl,
                userAgent = tvChannel.userAgent,
                title = tvChannel.title,
                banner = tvChannel.banner,
                position = tvChannel.position)
        }
    )
}
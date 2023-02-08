package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.TvChannels

interface OnTvChannelClickListener {
    fun onItemClick(tvChannel: TvChannels.TvChannel)
}
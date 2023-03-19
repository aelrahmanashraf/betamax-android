package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.TvChannels

interface OnTvChannelLongClickListener {
    fun onItemLongClick(tvChannel: TvChannels.TvChannel)
}
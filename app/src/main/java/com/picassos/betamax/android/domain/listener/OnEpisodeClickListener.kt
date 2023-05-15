package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.Episodes

interface OnEpisodeClickListener {
    fun onItemClick(episode: Episodes.Episode)
}
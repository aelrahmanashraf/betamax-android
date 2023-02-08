package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.ContinueWatching

interface OnContinueWatchingClickListener {
    fun onItemClick(continueWatching: ContinueWatching.ContinueWatching)
}
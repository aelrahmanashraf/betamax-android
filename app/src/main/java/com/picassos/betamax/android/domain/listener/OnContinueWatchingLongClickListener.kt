package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.ContinueWatching

interface OnContinueWatchingLongClickListener {
    fun onItemLongClick(continueWatching: ContinueWatching.ContinueWatching)
}
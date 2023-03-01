package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.ContinueWatching

interface OnContinueWatchingOptionsClickListener {
    fun onOptionsClick(continueWatching: ContinueWatching.ContinueWatching)
}
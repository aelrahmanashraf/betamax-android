package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.ContinueWatching

interface OnContinueWatchingFocusListener {
    fun onItemFocus(continueWatching: ContinueWatching.ContinueWatching)
}
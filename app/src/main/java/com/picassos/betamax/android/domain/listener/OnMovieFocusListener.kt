package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.Movies

interface OnMovieFocusListener {
    fun onItemFocus(movie: Movies.Movie)
}
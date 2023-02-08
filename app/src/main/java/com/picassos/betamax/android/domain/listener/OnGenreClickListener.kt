package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.Genres

interface OnGenreClickListener {
    fun onItemClick(genre: Genres.Genre)
}
package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.Movies

interface OnMovieClickListener {
    fun onItemClick(movie: Movies.Movie?)
}
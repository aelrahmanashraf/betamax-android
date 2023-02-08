package com.picassos.betamax.android.domain.listener

import com.picassos.betamax.android.domain.model.Seasons

interface OnSeasonClickListener {
    fun onItemClick(season: Seasons.Season)
}
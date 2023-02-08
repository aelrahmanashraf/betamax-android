package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.SeasonsDto
import com.picassos.betamax.android.domain.model.Seasons

fun SeasonsDto.toSeasons(): Seasons {
    return Seasons(
        seasons = seasons.map { season ->
            Seasons.Season(
                id = season.id,
                seasonId = season.seasonId,
                movieId = season.movieId,
                title = season.title,
                level = season.level)
        }
    )
}
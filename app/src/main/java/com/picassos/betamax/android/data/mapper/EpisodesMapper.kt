package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.EpisodesDto
import com.picassos.betamax.android.domain.model.Episodes

fun EpisodesDto.toEpisodes(): Episodes {
    return Episodes(
        seasonTitle = episodes.seasonTitle,
        rendered = episodes.rendered.map { episode ->
            Episodes.Episode(
                id = episode.id,
                episodeId = episode.episodeId,
                movieId = episode.movieId,
                level = episode.level,
                url = episode.url,
                title = episode.title,
                thumbnail = episode.thumbnail,
                duration = episode.duration)
        }
    )
}
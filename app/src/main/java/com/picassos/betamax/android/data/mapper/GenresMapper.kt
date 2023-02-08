package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.GenresDto
import com.picassos.betamax.android.domain.model.Genres

fun GenresDto.toGenres(): Genres {
    return Genres(
        genres = genres.map { genre ->
            Genres.Genre(
                id = genre.id,
                genreId = genre.genreId,
                title = genre.title,
                special = genre.special)
        }
    )
}
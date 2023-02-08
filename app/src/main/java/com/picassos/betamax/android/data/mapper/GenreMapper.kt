package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.GenreDto
import com.picassos.betamax.android.domain.model.Genre

fun GenreDto.toGenre(): Genre {
    return Genre(
        genreId = genre.details.genreId,
        title = genre.details.title)
}
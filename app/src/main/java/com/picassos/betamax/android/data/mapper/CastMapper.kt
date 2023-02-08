package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.CastDto
import com.picassos.betamax.android.domain.model.Cast

fun CastDto.toCast(): Cast {
    return Cast(
        cast = cast.map { cast ->
            Cast.Cast(
                id = cast.id,
                actorId = cast.actorId,
                name = cast.name,
                thumbnail = cast.thumbnail,
                role = cast.role,
                movieId = cast.movieId)
        }
    )
}
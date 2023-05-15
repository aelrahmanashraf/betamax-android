package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.ContinueWatchingDto
import com.picassos.betamax.android.domain.model.ContinueWatching

fun ContinueWatchingDto.toContinueWatching(): ContinueWatching {
    return ContinueWatching(
        continueWatching = continueWatching.map { continueWatching ->
            ContinueWatching.ContinueWatching(
                id = continueWatching.id,
                contentId = continueWatching.contentId,
                title = continueWatching.title,
                url = continueWatching.url,
                thumbnail = continueWatching.thumbnail,
                duration = continueWatching.duration,
                currentPosition = continueWatching.currentPosition,
                series = continueWatching.series)
        }
    )
}
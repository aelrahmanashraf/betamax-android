package com.picassos.betamax.android.data.mapper

import com.picassos.betamax.android.data.source.remote.dto.MoviesDto
import com.picassos.betamax.android.domain.model.Movies

fun MoviesDto.toMovies(): Movies {
    return Movies(
        movies = movies.map { movie ->
            Movies.Movie(
                id = movie.id,
                url = movie.url,
                genre = movie.genre,
                title = movie.title,
                description = movie.description,
                thumbnail = movie.thumbnail,
                banner = movie.banner,
                rating = movie.rating,
                duration = movie.duration,
                series = movie.series,
                featured = movie.featured,
                date = movie.date,
                currentPosition = movie.currentPosition)
        }
    )
}
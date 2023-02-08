package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class Genres(
    val genres: List<Genre>) {

    data class Genre(
        val id: Int,
        val genreId: Int,
        val title: String,
        val special: Int): Serializable
}
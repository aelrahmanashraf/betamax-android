package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class Movies(
    val movies: List<Movie>) {

    data class Movie(
        val id: Int,
        val url: String,
        val genre: Int = 0,
        val title: String,
        val description: String,
        val thumbnail: String,
        val banner: String,
        val rating: Double,
        val duration: Long?,
        val series: Int = 0,
        val featured: Int = 0,
        val date: String,
        val currentPosition: Int? = 0): Serializable
}
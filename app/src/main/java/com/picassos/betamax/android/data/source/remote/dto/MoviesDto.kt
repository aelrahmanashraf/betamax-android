package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class MoviesDto(
    @SerializedName("movies")
    val movies: List<Movie>) {

    data class Movie(
        @SerializedName("id")
        val id: Int,
        @SerializedName("url")
        val url: String,
        @SerializedName("genre")
        val genre: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("thumbnail")
        val thumbnail: String,
        @SerializedName("banner")
        val banner: String,
        @SerializedName("rating")
        val rating: Double,
        @SerializedName("duration")
        val duration: Long?,
        @SerializedName("series")
        val series: Int,
        @SerializedName("featured")
        val featured: Int,
        @SerializedName("date")
        val date: String,
        @SerializedName("current_position")
        val currentPosition: Int? = 0): java.io.Serializable
}
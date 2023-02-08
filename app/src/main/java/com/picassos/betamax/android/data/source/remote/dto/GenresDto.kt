package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class GenresDto(
    @SerializedName("genres")
    val genres: List<Genre>) {

    data class Genre(
        @SerializedName("id")
        val id: Int,
        @SerializedName("genre_id")
        val genreId: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("special")
        val special: Int): java.io.Serializable
}
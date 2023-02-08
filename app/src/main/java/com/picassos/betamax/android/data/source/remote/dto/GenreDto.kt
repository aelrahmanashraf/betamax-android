package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GenreDto(
    @SerializedName("genre")
    val genre: Genre) {

    data class Genre(
        @SerializedName("details")
        val details: Details) {

        data class Details(
            @SerializedName("genre_id")
            val genreId: Int,
            @SerializedName("title")
            val title: String): Serializable
    }
}
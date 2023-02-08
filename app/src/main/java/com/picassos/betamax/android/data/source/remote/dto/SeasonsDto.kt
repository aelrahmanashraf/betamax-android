package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class SeasonsDto(
    @SerializedName("seasons")
    val seasons: List<Season>) {

    data class Season(
        @SerializedName("id")
        val id: Int,
        @SerializedName("season_id")
        val seasonId: Int,
        @SerializedName("movie_id")
        val movieId: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("level")
        val level: Int): java.io.Serializable
}
package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class EpisodesDto(
    @SerializedName("episodes")
    val episodes: Episodes) {

    data class Episodes(
        @SerializedName("season_title")
        val seasonTitle: String,
        @SerializedName("rendered")
        val rendered: List<Rendered>) {

        data class Rendered(
            @SerializedName("id")
            val id: Int,
            @SerializedName("episode_id")
            val episodeId: Int,
            @SerializedName("movie_id")
            val movieId: Int,
            @SerializedName("level")
            val level: Int,
            @SerializedName("url")
            val url: String,
            @SerializedName("title")
            val title: String,
            @SerializedName("thumbnail")
            val thumbnail: String,
            @SerializedName("duration")
            val duration: Int)
    }
}
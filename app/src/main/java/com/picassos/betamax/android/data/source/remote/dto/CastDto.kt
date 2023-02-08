package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class CastDto(
    @SerializedName("cast")
    val cast: List<Cast>) {

    data class Cast(
        @SerializedName("id")
        val id: Int,
        @SerializedName("actor_id")
        val actorId: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("thumbnail")
        val thumbnail: String,
        @SerializedName("role")
        val role: String,
        @SerializedName("movie_id")
        val  movieId: Int): java.io.Serializable
}
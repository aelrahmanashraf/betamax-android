package com.picassos.betamax.android.domain.model

data class Cast(
    val cast: List<Cast>) {

    data class Cast(
        val id: Int,
        val actorId: Int,
        val name: String,
        val thumbnail: String,
        val role: String,
        val movieId: Int): java.io.Serializable
}
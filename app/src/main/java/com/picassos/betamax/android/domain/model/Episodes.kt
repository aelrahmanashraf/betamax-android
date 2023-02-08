package com.picassos.betamax.android.domain.model

data class Episodes(
    val seasonTitle: String,
    val rendered: List<Episode>) {

    data class Episode(
        val id: Int,
        val episodeId: Int,
        val movieId: Int,
        val level: Int,
        val url: String,
        val title: String,
        val thumbnail: String,
        val duration: Int): java.io.Serializable
}
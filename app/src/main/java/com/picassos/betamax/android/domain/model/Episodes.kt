package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class Episodes(
    val seasonTitle: String,
    val rendered: List<Episode>): Serializable {

    data class Episode(
        val id: Int,
        val episodeId: Int,
        val movieId: Int,
        val seasonLevel: Int,
        val level: Int,
        val url: String,
        val title: String,
        val thumbnail: String,
        val duration: Int,
        val currentPosition: Int?): Serializable
}
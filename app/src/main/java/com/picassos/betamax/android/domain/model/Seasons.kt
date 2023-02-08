package com.picassos.betamax.android.domain.model

data class Seasons(
    val seasons: List<Season>) {

    data class Season(
        val id: Int = 0,
        val seasonId: Int = 0,
        val movieId: Int = 0,
        val title: String = "",
        val level: Int = 0): java.io.Serializable
}
package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class TracksGroup(
    val data: List<Track>): Serializable {

    data class Track(
        val id: Int,
        val code: String,
        val title: String): Serializable
}
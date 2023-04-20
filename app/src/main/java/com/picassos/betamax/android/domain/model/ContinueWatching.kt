package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class ContinueWatching(
    val continueWatching: List<ContinueWatching>) {

    data class ContinueWatching(
        val id: Int,
        val contentId: Int,
        val title: String,
        val url: String,
        val thumbnail: String,
        val duration: Int,
        val currentPosition: Int): Serializable
}
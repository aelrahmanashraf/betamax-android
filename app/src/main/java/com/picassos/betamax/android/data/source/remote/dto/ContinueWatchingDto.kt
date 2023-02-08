package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

class ContinueWatchingDto(
    @SerializedName("continue_watching")
    val continueWatching: List<ContinueWatching>) {

        data class ContinueWatching(
            @SerializedName("id")
            val id: Int,
            @SerializedName("content_id")
            val contentId: Int,
            @SerializedName("url")
            val url: String,
            @SerializedName("thumbnail")
            val thumbnail: String,
            @SerializedName("current_position")
            val currentPosition: Int)
}
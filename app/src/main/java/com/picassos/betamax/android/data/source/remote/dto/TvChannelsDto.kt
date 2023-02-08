package com.picassos.betamax.android.data.source.remote.dto

import com.google.gson.annotations.SerializedName

data class TvChannelsDto(
    @SerializedName("tv_channels")
    val tvChannels: List<TvChannel>) {

    data class TvChannel(
        @SerializedName("id")
        val id: Int,
        @SerializedName("tvchannel_id")
        val tvChannelId: Int,
        @SerializedName("sd_url")
        val sdUrl: String,
        @SerializedName("hd_url")
        val hdUrl: String,
        @SerializedName("fhd_url")
        val fhdUrl: String,
        @SerializedName("user_agent")
        val userAgent: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("banner")
        val banner: String,
        @SerializedName("position")
        val position: Int = 0,): java.io.Serializable
}
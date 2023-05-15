package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class PlayerContent(
    val id: Int,
    val title: String,
    val url: String,
    val meta: String = "",
    val thumbnail: String = "",
    val currentPosition: Int = 0): Serializable
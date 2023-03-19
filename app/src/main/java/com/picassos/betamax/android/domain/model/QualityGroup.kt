package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class QualityGroup(
    val data: List<Quality>): Serializable {

    data class Quality(
        val id: Int,
        val prefix: String,
        val title: String): Serializable
}
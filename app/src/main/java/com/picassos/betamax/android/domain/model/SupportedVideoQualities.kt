package com.picassos.betamax.android.domain.model

import java.io.Serializable

data class SupportedVideoQualities(
    val sdQuality: Boolean = false,
    val hdQuality: Boolean = false,
    val fhdQuality: Boolean = false): Serializable
package com.picassos.betamax.android.core.configuration

import com.picassos.betamax.android.BuildConfig

object Config {

    // configuration
    const val LAUNCH_TIMEOUT = 1000L
    const val TV_LAUNCH_TIMEOUT = 3800L
    const val SLIDER_INTERVAL = 5000L
    const val TV_SLIDER_INTERVAL = 9000L
    const val PLAYER_REPLAY_DURATION = 30000L
    const val PLAYER_FORWARD_DURATION = 30000L

    // exoplayer
    const val MIN_BUFFER_DURATION = 32 * 1024
    const val MAX_BUFFER_DURATION = 64 * 1024
    const val MIN_PLAYBACK_START_BUFFER = 1024
    const val MIN_PLAYBACK_RESUME_BUFFER = 1024

    // build preferences
    const val BUILD_TYPE = BuildConfig.BUILD_TYPE
    const val MOCK_TV = true
}
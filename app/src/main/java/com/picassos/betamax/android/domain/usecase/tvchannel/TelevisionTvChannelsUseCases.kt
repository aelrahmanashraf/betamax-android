package com.picassos.betamax.android.domain.usecase.tvchannel

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.genre.GetTvGenresUseCase
import com.picassos.betamax.android.domain.usecase.video_quality.GetVideoQualityUseCase
import javax.inject.Inject

data class TelevisionTvChannelsUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getTvGenresUseCase: GetTvGenresUseCase,
    val getTvChannelsUseCase: GetTvChannelsUseCase,
    val getTvChannelsByGenreUseCase: GetTvChannelsByGenreUseCase,
    val getVideoQualityUseCase: GetVideoQualityUseCase,
    val getSavedTvChannelsUseCase: GetSavedTvChannelsUseCase,
    val saveTvChannelUseCase: SaveTvChannelUseCase)
package com.picassos.betamax.android.domain.usecase.tvchannel

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.genre.GetTvGenresUseCase
import com.picassos.betamax.android.domain.usecase.video_quality.GetVideoQualityUseCase
import javax.inject.Inject

data class ViewTvUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getTvChannelUseCase: GetTvChannelUseCase,
    val getTvGenresUseCase: GetTvGenresUseCase,
    val getTvChannelsUseCase: GetTvChannelsUseCase,
    val getTvChannelsByGenreUseCase: GetTvChannelsByGenreUseCase,
    val getVideoQualityUseCase: GetVideoQualityUseCase)
package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.episode.GetEpisodesUseCase
import com.picassos.betamax.android.domain.usecase.season.GetSeasonsUseCase
import javax.inject.Inject

data class ViewMovieUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getMovieUseCase: GetMovieUseCase,
    val saveMovieUseCase: SaveMovieUseCase)
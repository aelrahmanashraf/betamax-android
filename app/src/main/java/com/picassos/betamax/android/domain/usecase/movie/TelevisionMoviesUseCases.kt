package com.picassos.betamax.android.domain.usecase.movie

import com.picassos.betamax.android.domain.usecase.account.auth.GetLocalAccountUseCase
import com.picassos.betamax.android.domain.usecase.genre.GetAllGenresUseCase
import javax.inject.Inject

data class TelevisionMoviesUseCases @Inject constructor(
    val getLocalAccountUseCase: GetLocalAccountUseCase,
    val getAllGenresUseCase: GetAllGenresUseCase,
    val getMoviesUseCase: GetMoviesUseCase,
    val getNewlyReleaseMoviesUseCase: GetNewlyReleaseMoviesUseCase,
    val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    val getMoviesByGenreUseCase: GetMoviesByGenreUseCase,
    val searchMoviesUseCase: SearchMoviesUseCase)
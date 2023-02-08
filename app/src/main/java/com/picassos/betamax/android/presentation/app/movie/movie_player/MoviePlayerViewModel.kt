package com.picassos.betamax.android.presentation.app.movie.movie_player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.picassos.betamax.android.domain.usecase.movie_player.MoviePlayerUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoviePlayerViewModel @Inject constructor(app: Application, private val moviePlayerUseCases: MoviePlayerUseCases): AndroidViewModel(app) {

}
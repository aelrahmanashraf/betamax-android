package com.picassos.betamax.android.presentation.television.movie.movie_player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TelevisionMoviePlayerViewModel @Inject constructor(app: Application): AndroidViewModel(app)
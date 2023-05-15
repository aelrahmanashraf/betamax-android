package com.picassos.betamax.android.presentation.television.episode.episode_player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TelevisionEpisodePlayerViewModel @Inject constructor(app: Application): AndroidViewModel(app)
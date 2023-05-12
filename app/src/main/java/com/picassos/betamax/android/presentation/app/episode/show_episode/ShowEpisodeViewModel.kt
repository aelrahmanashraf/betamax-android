package com.picassos.betamax.android.presentation.app.episode.show_episode

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.picassos.betamax.android.domain.usecase.episode.ShowEpisodeUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShowEpisodeViewModel @Inject constructor(app: Application, private val showEpisodeUseCases: ShowEpisodeUseCases): AndroidViewModel(app)
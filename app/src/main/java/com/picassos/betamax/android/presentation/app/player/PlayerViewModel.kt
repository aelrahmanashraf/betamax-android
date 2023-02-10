package com.picassos.betamax.android.presentation.app.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class PlayerStatus {
    INITIALIZE, PREPARE, PLAY, PAUSE, RETRY
}

@HiltViewModel
class PlayerViewModel @Inject constructor(app: Application): AndroidViewModel(app) {
    private val _playerStatus = MutableStateFlow(PlayerStatus.INITIALIZE)
    val playerStatus = _playerStatus.asStateFlow()

    fun setPlayerStatus(status: PlayerStatus) {
        _playerStatus.tryEmit(status)
    }
}
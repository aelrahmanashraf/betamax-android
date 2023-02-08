package com.picassos.betamax.android.presentation.television.tvchannel.tvchannel_player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.tvchannel.GetSingleTvChannelUseCase
import com.picassos.betamax.android.domain.usecase.tvchannel.GetTvChannelUseCase
import com.picassos.betamax.android.presentation.app.tvchannel.TvChannelState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelevisionTvChannelPlayerViewModel @Inject constructor(app: Application, val getSingleTvChannelUseCase: GetSingleTvChannelUseCase): AndroidViewModel(app) {
    private val _tvChannel = MutableStateFlow(TvChannelState())
    val tvChannel = _tvChannel.asStateFlow()

    fun requestTvChannel(tvChannelId: Int) {
        viewModelScope.launch {
            getSingleTvChannelUseCase(tvChannelId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _tvChannel.emit(TvChannelState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _tvChannel.emit(TvChannelState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _tvChannel.emit(TvChannelState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
package com.picassos.betamax.android.presentation.app.tvchannel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.tvchannel.TvUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvViewModel @Inject constructor(app: Application, private val tvUseCases: TvUseCases): AndroidViewModel(app) {
    private val _tvChannels = MutableStateFlow(TvState())
    val tvChannels = _tvChannels.asStateFlow()

    fun requestTvChannels() {
        viewModelScope.launch {
            tvUseCases.getTvChannelsUseCase.invoke().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _tvChannels.emit(TvState(
                            isLoading = result.isLoading))
                    }
                    is Resource.Success -> {
                        _tvChannels.emit(TvState(
                            response = result.data))
                    }
                    is Resource.Error -> {
                        _tvChannels.emit(TvState(
                            error = result.message))
                    }
                }
            }
        }
    }
}
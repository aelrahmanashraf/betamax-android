package com.picassos.betamax.android.presentation.app.quality.manage_video_quality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.picassos.betamax.android.core.resource.Resource
import com.picassos.betamax.android.domain.usecase.video_quality.VideoQualityUseCases
import com.picassos.betamax.android.presentation.app.quality.update_video_quality.UpdateVideoQualityState
import com.picassos.betamax.android.presentation.app.quality.QualityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageVideoQualityViewModel @Inject constructor(app: Application, private val videoQualityUseCases: VideoQualityUseCases): AndroidViewModel(app) {
    private val _videoQuality = MutableStateFlow(QualityState())
    val videoQuality = _videoQuality.asStateFlow()

    fun requestVideoQuality() {
        viewModelScope.launch {
            videoQualityUseCases.getLocalAccountUseCase.invoke().collect { account ->
                videoQualityUseCases.getVideoQualityUseCase(account.token).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _videoQuality.emit(QualityState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _videoQuality.emit(QualityState(
                                response = result.data))
                        }
                        is Resource.Error -> {
                            _videoQuality.emit(QualityState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }

    private val _updateVideoQuality = MutableStateFlow(UpdateVideoQualityState())
    val updateVideoQuality = _updateVideoQuality.asStateFlow()

    fun requestUpdateVideoQuality(quality: Int) {
        viewModelScope.launch {
            videoQualityUseCases.getLocalAccountUseCase.invoke().collect { account ->
                videoQualityUseCases.updateVideoQualityUseCase(account.token, quality).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _updateVideoQuality.emit(UpdateVideoQualityState(
                                isLoading = result.isLoading))
                        }
                        is Resource.Success -> {
                            _updateVideoQuality.emit(UpdateVideoQualityState(
                                responseCode = result.data))
                        }
                        is Resource.Error -> {
                            _updateVideoQuality.emit(UpdateVideoQualityState(
                                error = result.message))
                        }
                    }
                }
            }
        }
    }
}
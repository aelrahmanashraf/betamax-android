package com.picassos.betamax.android.presentation.app.quality.video_quality_chooser

import androidx.lifecycle.ViewModel
import com.picassos.betamax.android.domain.model.SupportedVideoQualities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VideoQualityChooserViewModel @Inject constructor(): ViewModel() {
    private val _supportedVideoQualities = MutableStateFlow(SupportedVideoQualities())
    val supportedVideoQualities = _supportedVideoQualities.asStateFlow()

    fun setSupportedQualities(qualities: SupportedVideoQualities) {
        _supportedVideoQualities.tryEmit(qualities)
    }

    private val _selectedVideoQuality = MutableStateFlow<Int?>(null)
    val selectedVideoQuality = _selectedVideoQuality.asStateFlow()

    fun setVideoQuality(videoQuality: Int?) {
        _selectedVideoQuality.tryEmit(videoQuality)
    }
}
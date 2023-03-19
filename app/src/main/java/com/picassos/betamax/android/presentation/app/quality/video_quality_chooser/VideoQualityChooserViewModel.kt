package com.picassos.betamax.android.presentation.app.quality.video_quality_chooser

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VideoQualityChooserViewModel @Inject constructor(): ViewModel() {
    private val _selectedVideoQuality = MutableStateFlow<Int?>(null)
    val selectedVideoQuality = _selectedVideoQuality.asStateFlow()

    fun setVideoQuality(videoQuality: Int?) {
        _selectedVideoQuality.tryEmit(videoQuality)
    }
}
package com.picassos.betamax.android.presentation.app.video_quality.video_quality_chooser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picassos.betamax.android.R
import com.picassos.betamax.android.databinding.VideoQualityChooserBottomSheetModalBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoQualityChooserBottomSheetModal : BottomSheetDialogFragment() {
    private lateinit var layout: VideoQualityChooserBottomSheetModalBinding
    private val videoQualityChooserViewModel: VideoQualityChooserViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.video_quality_chooser_bottom_sheet_modal, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout.apply {
            sdQuality.setOnClickListener {
                videoQualityChooserViewModel.setVideoQuality(1)
                dismiss()
            }
            hdQuality.setOnClickListener {
                videoQualityChooserViewModel.setVideoQuality(2)
                dismiss()
            }
            fhdQuality.setOnClickListener {
                videoQualityChooserViewModel.setVideoQuality(3)
                dismiss()
            }
        }
    }
}
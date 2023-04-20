package com.picassos.betamax.android.presentation.app.quality.video_quality_chooser

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Helper
import com.picassos.betamax.android.core.utilities.Helper.getBundleSerializable
import com.picassos.betamax.android.databinding.VideoQualityChooserBottomSheetModalBinding
import com.picassos.betamax.android.domain.model.SupportedVideoQualities
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

        getBundleSerializable(requireArguments(), "qualities", SupportedVideoQualities::class.java).also { qualities ->
            layout.sdQuality.apply {
                visibility = when (qualities.sdQuality) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                setOnClickListener {
                    videoQualityChooserViewModel.setVideoQuality(1)
                    dismiss()
                }
            }
            layout.hdQuality.apply {
                visibility = when (qualities.hdQuality) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                setOnClickListener {
                    videoQualityChooserViewModel.setVideoQuality(2)
                    dismiss()
                }
            }
            layout.fhdQuality.apply {
                visibility = when (qualities.fhdQuality) {
                    true -> View.VISIBLE
                    else -> View.GONE
                }
                setOnClickListener {
                    videoQualityChooserViewModel.setVideoQuality(3)
                    dismiss()
                }
            }
        }
    }

    private fun setBehaviorState(state: Int) {
        BottomSheetBehavior.from(requireView().parent as View).apply behavior@ {
            this@behavior.state = state
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        setBehaviorState(state = BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onStart() {
        super.onStart()
        setBehaviorState(state = BottomSheetBehavior.STATE_EXPANDED)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Helper.hideSystemUI(requireActivity().window, layout.root)
        }
    }
}
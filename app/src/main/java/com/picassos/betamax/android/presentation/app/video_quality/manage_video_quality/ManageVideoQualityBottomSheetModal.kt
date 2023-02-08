package com.picassos.betamax.android.presentation.app.video_quality.manage_video_quality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.picassos.betamax.android.R
import com.picassos.betamax.android.core.utilities.Coroutines.collectLatestOnLifecycleSheetOwnerStarted
import com.picassos.betamax.android.core.view.Toasto.showToast
import com.picassos.betamax.android.databinding.ManageVideoQualityBottomSheetModalBinding
import com.picassos.betamax.android.core.utilities.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageVideoQualityBottomSheetModal : BottomSheetDialogFragment() {
    private lateinit var layout: ManageVideoQualityBottomSheetModalBinding
    private val manageViewQualityViewModel: ManageVideoQualityViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.manage_video_quality_bottom_sheet_modal, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manageViewQualityViewModel.requestVideoQuality()
        collectLatestOnLifecycleSheetOwnerStarted(manageViewQualityViewModel.videoQuality) { state ->
            if (state.response != null) {
                when (state.response) {
                    1 -> layout.sdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background_selected)
                    2 -> layout.hdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background_selected)
                    3 -> layout.fhdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background_selected)
                }
            }
            if (state.error != null) {
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(requireContext(), getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }

        layout.apply {
            sdQuality.setOnClickListener {
                manageViewQualityViewModel.requestUpdateVideoQuality(1)
                sdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background_selected)
                hdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background)
                fhdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background)
            }
            hdQuality.setOnClickListener {
                manageViewQualityViewModel.requestUpdateVideoQuality(2)
                sdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background)
                hdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background_selected)
                fhdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background)
            }
            fhdQuality.setOnClickListener {
                manageViewQualityViewModel.requestUpdateVideoQuality(3)
                sdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background)
                hdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background)
                fhdQuality.background = requireContext().getDrawable(R.drawable.input_rectangle_background_selected)
            }
        }

        collectLatestOnLifecycleSheetOwnerStarted(manageViewQualityViewModel.updateVideoQuality) { state ->
            if (state.responseCode != null) {
                when (state.responseCode) {
                    200 -> { }
                    else -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
            if (state.error != null) {
                when (state.error) {
                    Response.NETWORK_FAILURE_EXCEPTION -> {
                        showToast(requireContext(), getString(R.string.please_check_your_internet_connection_and_try_again), 0, 1)
                    }
                    Response.MALFORMED_REQUEST_EXCEPTION -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                        Firebase.crashlytics.log("Request returned a malformed request or response.")
                    }
                    else -> {
                        showToast(requireContext(), getString(R.string.unknown_issue_occurred), 0, 1)
                    }
                }
            }
        }
    }
}
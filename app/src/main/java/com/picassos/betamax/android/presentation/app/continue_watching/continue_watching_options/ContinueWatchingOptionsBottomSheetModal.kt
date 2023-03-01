package com.picassos.betamax.android.presentation.app.continue_watching.continue_watching_options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picassos.betamax.android.R
import com.picassos.betamax.android.databinding.ContinueWatchingOptionsBottomSheetModalBinding
import com.picassos.betamax.android.presentation.app.continue_watching.ContinueWatchingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContinueWatchingOptionsBottomSheetModal : BottomSheetDialogFragment() {
    private lateinit var layout: ContinueWatchingOptionsBottomSheetModalBinding
    private val continueWatchingViewModel: ContinueWatchingViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.continue_watching_options_bottom_sheet_modal, container, false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout.apply {
            removeContinueWatching.setOnClickListener {
                requireArguments().getInt("contentId").let { contentId ->
                    continueWatchingViewModel.requestDeleteContinueWatching(contentId = contentId)
                    dismiss()
                }
            }
        }
    }
}